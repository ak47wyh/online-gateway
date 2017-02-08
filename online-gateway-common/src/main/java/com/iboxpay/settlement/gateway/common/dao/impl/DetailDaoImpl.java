package com.iboxpay.settlement.gateway.common.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.dao.DetailDao;
import com.iboxpay.settlement.gateway.common.domain.DetailEntity;
import com.iboxpay.settlement.gateway.common.domain.DetailQueryRecordEntity;
import com.iboxpay.settlement.gateway.common.page.PageBean;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.MD5;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Component
public class DetailDaoImpl extends BaseDaoImpl<DetailEntity> implements DetailDao {

    private static Logger logger = LoggerFactory.getLogger(DetailDaoImpl.class);

    @Override
    public PageBean queryDetails(String accNo, String customerAccNo, String customerAccName, BigDecimal beginAmount, BigDecimal endAmount, boolean isQueryCredit, Date beginDate, Date endDate,
            int pageSize, int pageNo) {
        List<Object> params = new ArrayList<Object>();
        Date nextDay = DateTimeUtil.addDay(endDate, 1);//到第二天凌晨的00:00
        StringBuilder hql = new StringBuilder("from DetailEntity " + "where accNo=?" + " and transDate>=?" + " and transDate<? ");
        params.add(accNo);
        params.add(beginDate);
        params.add(nextDay);
        if (!StringUtils.isBlank(customerAccNo)) {
            hql.append(" and customerAccNo = ?");
            params.add(customerAccNo);
        }
        if (!StringUtils.isBlank(customerAccName)) {
            hql.append(" and customerAccName = ?");
            params.add(customerAccName);
        }
        if (isQueryCredit) {
            if (beginAmount != null) {
                hql.append(" and creditAmount >= ?");
                params.add(beginAmount);
            }
            if (endAmount != null) {
                hql.append(" and creditAmount <= ?");
                params.add(endAmount);
            }
        } else {
            if (beginAmount != null) {
                hql.append(" and debitAmount >= ?");
                params.add(beginAmount);
            }
            if (endAmount != null) {
                hql.append(" and debitAmount <= ?");
                params.add(endAmount);
            }
        }
        hql.append("order by transDate asc, orderIndex asc");
        return findPage(pageNo, pageSize, hql.toString(), params.toArray(new Object[0]));
    }

    public void saveDetails(DetailEntity[] detailEntitys, String accNo, Date beginDate, Date endDate) {
        Date now = new Date();

        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query q = session.createQuery("from DetailEntity where " + "fieldsHash=:fieldsHash " + "and updateTime<>:updateTime " + //更新时间用来控制一条记录不能更新多次（如果有多个相同的结果）
                    "order by orderIndex asc");
            int i = 0;
            for (DetailEntity detailEntity : detailEntitys) {
                String fieldHash = getKeyFieldsHash(detailEntity);
                q.setString("fieldsHash", fieldHash);
                q.setTimestamp("updateTime", now);
                List list = q.list();
                if (list.size() == 0) {
                    detailEntity.setCreateTime(now);
                    detailEntity.setUpdateTime(now);
                    detailEntity.setFieldsHash(fieldHash);
                    save(detailEntity);//不存在，直接插入
                } else {//可能多于一个结果
                    DetailEntity existDetailEntity = (DetailEntity) list.get(0);
                    detailEntity.setFieldsHash(fieldHash);
                    detailEntity.setId(existDetailEntity.getId());
                    detailEntity.setCreateTime(existDetailEntity.getCreateTime());
                    detailEntity.setUpdateTime(now);
                    update(detailEntity);
                }
                if (++i % 50 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    private String getKeyFieldsHash(DetailEntity detailEntity) {
        String keyFieldStr =
                new StringBuilder().append(detailEntity.getAccNo()).append("_").append(detailEntity.getCustomerAccNo()).append("_").append(detailEntity.getDebitAmount().toString()).append("_")
                        .append(detailEntity.getCreditAmount().toString()).append("_").append(detailEntity.getTransDate().getTime()).toString();
        return MD5.encode(keyFieldStr);
    }

    //删除旧的 当日明细查询 记录
    private void tryDelOldTodayRecord(String accNo, Date beginDate, Date endDate) {
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query q = session.createQuery("delete from DetailQueryRecordEntity " + "where accNo=:accNo " + "and detailDay >= :beginDate " + "and detailDay <= :endDate " + "and type = 0");
            q.setDate("beginDate", beginDate);
            q.setDate("endDate", endDate);
            q.setString("accNo", accNo);
            q.executeUpdate();
            tx.commit();
        } finally {
            session.close();
        }
    }

    public void updateQueryRecords(String accNo, Date beginDate, Date endDate, int type) {
        if (type == DetailQueryRecordEntity.TYPE_HISTORY) tryDelOldTodayRecord(accNo, beginDate, endDate);

        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Date now = new Date();
            //查询原有的查询记录.从小到大排序.
            DetailQueryRecordEntity[] queryRecords = getQueryRecords(session, accNo, beginDate, endDate, type);
            //查找未查询过的间隙并记录下来

            Date day = beginDate;
            int index = -1;
            int size = -1;
            if (queryRecords != null && queryRecords.length > 0) {
                index = 0;
                size = queryRecords.length;
            }
            for (;;) {
                if (index >= 0 && index < size) {
                    if (queryRecords[index].getDetailDay().getTime() >= day.getTime()) {
                        queryRecords[index].setUpdateTime(now);
                        session.update(queryRecords[index]);
                        index++;
                    }
                } else {//不存在的，插入.
                    DetailQueryRecordEntity queryRecord = new DetailQueryRecordEntity();
                    queryRecord.setAccNo(accNo);
                    queryRecord.setType(type);
                    queryRecord.setDetailDay(day);
                    queryRecord.setCreateTime(now);
                    queryRecord.setUpdateTime(now);
                    session.save(queryRecord);
                }
                day = DateTimeUtil.addDay(day, 1);
                if (day.getTime() > endDate.getTime()) break;
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    //用于辅助合并相交日期
    @Override
    public DetailQueryRecordEntity[] getAccountQueryRecords(String accNo, Date beginDate, Date endDate) {
        Session session = getSession();
        try {
            return getQueryRecords(session, accNo, beginDate, endDate, -1);
        } finally {
            session.close();
        }
    }

    private DetailQueryRecordEntity[] getQueryRecords(Session session, String accNo, Date beginDate, Date endDate, int type) {
        Query q =
                session.createQuery("from DetailQueryRecordEntity " + "where accNo=:accNo " + (type != -1 ? "and type=:type " : "") + "and detailDay>=:beginDate " + "and detailDay<=:endDate "
                        + "order by detailDay asc");
        q.setString("accNo", accNo);
        if (type != -1) q.setInteger("type", type);
        q.setDate("beginDate", beginDate);
        q.setDate("endDate", endDate);
        List list = q.list();
        return (DetailQueryRecordEntity[]) list.toArray(new DetailQueryRecordEntity[0]);
    }

    @Override
    public List<Date[]> determineActualQueryDate(String accNo, Date beginDate, Date endDate) {
        Session session = getSession();
        List<Date[]> result = new ArrayList<Date[]>();
        try {
            DetailQueryRecordEntity[] queryRecords = getQueryRecords(session, accNo, beginDate, endDate, DetailQueryRecordEntity.TYPE_HISTORY);
            if (queryRecords == null || queryRecords.length == 0) {//没查询过，直接返回开始结束日期
                result.add(new Date[] { beginDate, endDate });
            } else {
                Date date = beginDate;
                for (int i = 0; i < queryRecords.length; i++) {
                    DetailQueryRecordEntity queryRecord = queryRecords[i];
                    if (i == 0 && date.getTime() < queryRecord.getDetailDay().getTime()) {//开始是8-22,但查询的是8-24，会产生2天
                        result.add(new Date[] { date, DateTimeUtil.addDay(queryRecord.getDetailDay(), -1) });
                    } else if (queryRecord.getDetailDay().getTime() > DateTimeUtil.addDay(date, 1).getTime()) {//间隔超过一天
                        result.add(new Date[] { DateTimeUtil.addDay(date, 1), DateTimeUtil.addDay(queryRecord.getDetailDay(), -1) });
                    } else if (i == queryRecords.length - 1 && queryRecord.getDetailDay().getTime() < endDate.getTime()) {//最后一个
                        result.add(new Date[] { DateTimeUtil.addDay(queryRecord.getDetailDay(), 1), endDate });
                    }
                    date = queryRecord.getDetailDay();
                }
            }
        } finally {
            session.close();
        }
        return result;
    }

    @Override
    public DetailQueryRecordEntity getTodayQueryRecord(String accNo) {
        Date now = DateTimeUtil.truncateTime(new Date());
        Session session = getSession();
        try {
            Query q = session.createQuery("from DetailQueryRecordEntity where accNo=:accNo and detailDay=:endDate and type=0");
            q.setString("accNo", accNo);
            q.setDate("endDate", now);
            return (DetailQueryRecordEntity) q.uniqueResult();
        } finally {
            session.close();
        }
    }
    //add it by caolipeng at 2015-06-02 start
	@Override
	public List<DetailEntity> getDetailList(String accNo, Date beginDate,
			Date endDate) {
		Session session = getSession();
        try {
            return getQueryRecords(session, accNo, beginDate, endDate);
        } finally {
            session.close();
        }
	}
	
	private List<DetailEntity> getQueryRecords(Session session, String accNo, Date beginDate, Date endDate) {
		//由于前端返回来的Date日期只有年月日，时分秒都为0.故查询的时候，需要处理一下。T+1
		Date eDate = DateTimeUtil.addDay(endDate, 1);
		Query q =
                session.createQuery("from DetailEntity " + "where accNo=:accNo "+"and transDate>=:beginDate " + "and transDate<=:endDate "
                        + "order by transDate asc");
        q.setString("accNo", accNo);
        q.setDate("beginDate", beginDate);
        q.setDate("endDate", eDate);
        List<DetailEntity> list = q.list();
        return list;
    }
	//add it by caolipeng at 2015-06-02 end
}
