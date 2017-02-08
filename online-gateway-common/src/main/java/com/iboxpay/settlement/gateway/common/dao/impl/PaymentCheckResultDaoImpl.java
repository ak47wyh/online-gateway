package com.iboxpay.settlement.gateway.common.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.dao.PaymentCheckResultDao;
import com.iboxpay.settlement.gateway.common.domain.PaymentCheckRecordEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentCheckResultEntity;
import com.iboxpay.settlement.gateway.common.page.PageBean;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Component
public class PaymentCheckResultDaoImpl extends BaseDaoImpl<PaymentCheckRecordEntity> implements PaymentCheckResultDao {

    @Override
    public void save(String accNo, Date checkDate, List<PaymentCheckResultEntity> checkResultList) {
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Date now = new Date();
            int i = 0;
            if (checkResultList != null) {
                Query q = session.createQuery("from PaymentCheckResultEntity where paymentId = :paymentId");
                for (PaymentCheckResultEntity paymentCheckResult : checkResultList) {
                    q.setLong("paymentId", paymentCheckResult.getPaymentId());
                    PaymentCheckResultEntity _paymentCheckResult = (PaymentCheckResultEntity) q.uniqueResult();
                    if (_paymentCheckResult == null) {
                        paymentCheckResult.setCreateTime(now);
                        paymentCheckResult.setUpdateTime(now);
                        session.save(paymentCheckResult);
                    } else {
                        _paymentCheckResult.setCheckStatus(paymentCheckResult.getCheckStatus());
                        _paymentCheckResult.setCheckStatusMsg(paymentCheckResult.getCheckStatusMsg());
                        _paymentCheckResult.setDetailId(paymentCheckResult.getDetailId());
                        _paymentCheckResult.setUpdateTime(now);
                        session.update(_paymentCheckResult);
                    }
                    if (++i % 50 == 0) {
                        session.flush();
                        session.clear();
                    }
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
    public PageBean queryCheckResults(String accNo, String customerAccNo, String customerAccName, BigDecimal beginAmount, BigDecimal endAmount, Date transDate, String hasCheck, int status,
            int pageSize, int pageNo) {
        Session session = getSession();
        try {
            List<Object> params = new ArrayList<Object>();
            Date nextDay = DateTimeUtil.addDay(transDate, 1);//到第二天凌晨的00:00
            StringBuilder where = new StringBuilder();
            String join = "from T_EB_Payment p left join T_EB_PaymentCheckResult pr on p.id=pr.payment_Id ";//hql在实体没关联时，不支持left join
            StringBuilder hql =
                    new StringBuilder("select " + "p.batch_Seq_Id as batchSeqId, " + "p.seq_Id as seqId, " + "p.id as paymentId," + "pr.detail_Id as detailId, " + "p.acc_No as accNo,"
                            + "p.amount as amount," + "p.submit_Pay_Time as transTime," + "p.customer_Acc_No as customerAccNo," + "p.customer_Acc_Name as customerAccName,"
                            + "p.customer_Bank_Full_Name as customerBankFullName," + "p.status as status," + "p.status_Msg as statusMsg," +

                            "p.pay_Bank_Status as payBankStatus," + "p.pay_Bank_Status_Msg as payBankStatusMsg," + "p.bank_Status as bankStatus," + "p.bank_Status_Msg as bankStatusMsg," +

                            "pr.check_Status as checkStatus," + "pr.check_Status_Msg as checkStatusMsg," + "pr.create_Time as createTime," + "pr.update_Time as updateTime " + join);

            where.append("where p.acc_No=?" + " and p.trans_Date>=?" + " and p.trans_Date<? ");
            params.add(accNo);
            params.add(transDate);
            params.add(nextDay);
            if (!StringUtils.isBlank(customerAccNo)) {
                where.append(" and p.customer_Acc_No = ?");
                params.add(customerAccNo);
            }
            if (!StringUtils.isBlank(customerAccName)) {
                where.append(" and p.customer_Acc_Name = ?");
                params.add(customerAccName);
            }
            if (beginAmount != null) {
                where.append(" and p.amount >= ?");
                params.add(beginAmount);
            }
            if (endAmount != null) {
                where.append(" and p.amount <= ?");
                params.add(endAmount);
            }
            if (status == -1) {
                where.append(" and p.status >= ?");
                params.add(PaymentStatus.STATUS_SUBMITTED);//默认只对已经提交的
            } else if (status > 0) {
                where.append(" and p.status = ?");
                params.add(status);
            }
            if ("unchecked".equals(hasCheck)) {
                where.append(" and pr.check_Status is null");
            } else if ("checked".equals(hasCheck)) {
                where.append(" and pr.check_Status > ?");
                params.add(0);
            }
            hql.append(where);
            hql.append(" order by p.id asc");

            Query q = session.createSQLQuery("select count(*) as c " + join + where.toString()).addScalar("c", Hibernate.LONG);//不设置这个，会返回BigDecimal
            for (int i = 0; i < params.size(); i++) {
                q.setParameter(i, params.get(i));
            }
            Long totalCount = (Long) q.uniqueResult();

            PageBean pb = new PageBean(pageNo, pageSize, totalCount);
            List result = null;
            if (totalCount > 0) {
                q =
                        session.createSQLQuery(hql.toString())
                                //囧，10G方言里没有NVARCHAR的映射，报错：No Dialect mapping for JDBC type: -9
                                .addScalar("batchSeqId", Hibernate.STRING).addScalar("seqId", Hibernate.STRING).addScalar("paymentId", Hibernate.LONG).addScalar("detailId", Hibernate.LONG)
                                .addScalar("accNo", Hibernate.STRING).addScalar("amount", Hibernate.BIG_DECIMAL).addScalar("transTime", Hibernate.TIMESTAMP)
                                .addScalar("customerAccNo", Hibernate.STRING).addScalar("customerAccName", Hibernate.STRING).addScalar("status", Hibernate.INTEGER)
                                .addScalar("statusMsg", Hibernate.STRING).addScalar("payBankStatus", Hibernate.STRING).addScalar("payBankStatusMsg", Hibernate.STRING)
                                .addScalar("bankStatus", Hibernate.STRING).addScalar("bankStatusMsg", Hibernate.STRING).addScalar("checkStatus", Hibernate.INTEGER)
                                .addScalar("createTime", Hibernate.TIMESTAMP).addScalar("updateTime", Hibernate.TIMESTAMP).addScalar("customerBankFullName", Hibernate.STRING)
                                .addScalar("checkStatusMsg", Hibernate.STRING).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
                for (int i = 0; i < params.size(); i++) {
                    q.setParameter(i, params.get(i));
                }
                q.setFirstResult(pb.getStartIndex());
                q.setMaxResults(pb.getPageSize());
                result = q.list();
            }
            pb.setResult(result);
            return pb;
        } finally {
            session.close();
        }
    }

}
