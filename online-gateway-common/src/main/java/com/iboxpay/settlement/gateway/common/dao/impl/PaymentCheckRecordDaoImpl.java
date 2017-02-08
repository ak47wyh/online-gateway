package com.iboxpay.settlement.gateway.common.dao.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.dao.PaymentCheckRecordDao;
import com.iboxpay.settlement.gateway.common.domain.PaymentCheckRecordEntity;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;

@Component
public class PaymentCheckRecordDaoImpl extends BaseDaoImpl<PaymentCheckRecordEntity> implements PaymentCheckRecordDao {

    @Override
    public boolean hasCheck(String accNo, Date checkDate) {
        Session session = getSession();
        try {
            List<PaymentCheckRecordEntity> list = queryChecked(accNo, checkDate, session);
            return list != null && list.size() > 0;
        } finally {
            session.close();
        }
    }

    private List<PaymentCheckRecordEntity> queryChecked(String accNo, Date checkDate, Session session) {
        checkDate = DateTimeUtil.truncateTime(checkDate);
        Query q = session.createQuery("from PaymentCheckRecordEntity where accNo=:accNo and checkDay=:checkDay");
        q.setString("accNo", accNo);
        q.setDate("checkDay", checkDate);
        List<PaymentCheckRecordEntity> list = q.list();
        return list;
    }

    @Override
    public void setCheck(String accNo, Date checkDate) {
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List<PaymentCheckRecordEntity> list = queryChecked(accNo, checkDate, session);
            Date now = new Date();
            if (list == null || list.size() == 0) {
                PaymentCheckRecordEntity checkRecord = new PaymentCheckRecordEntity();
                checkRecord.setAccNo(accNo);
                checkRecord.setCheckDay(checkDate);
                checkRecord.setCreateTime(now);
                checkRecord.setUpdateTime(now);
                session.save(checkRecord);
            } else {
                for (PaymentCheckRecordEntity checkRecord : list) {//并发可能出现多个
                    checkRecord.setUpdateTime(now);
                    session.update(checkRecord);
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

    @Override
    public PaymentCheckRecordEntity getCheckRecord(String accNo, Date checkDate) {
        Session session = getSession();
        try {
            List<PaymentCheckRecordEntity> list = queryChecked(accNo, checkDate, session);
            if (list != null && list.size() > 0)
                return (PaymentCheckRecordEntity) list.get(0);
            else return null;
        } finally {
            session.close();
        }
    }

}
