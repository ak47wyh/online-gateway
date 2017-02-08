package com.iboxpay.settlement.gateway.common.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.domain.BatchPaymentEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;

@Component("paymentDao")
public class PaymentDaoImpl extends BaseDaoImpl<PaymentEntity> implements PaymentDao {

    //	@Override
    //	public PaymentEntity getBySettleId(String settleId) {
    //		List<PaymentEntity> list = getHibernateTemplate().find("from PaymentEntity p where p.settleId = ?", settleId);
    //		if(list.size() == 1){
    //			return (PaymentEntity)list.get(0);
    //		}else if(list.size() > 1){
    //			long lastSubmitTime = 0;
    //			long submitTime;
    //			PaymentEntity lastPaymentEntity = null;
    //			for(PaymentEntity p : list){
    //				submitTime = p.getCreateTime().getTime();
    //				if(lastSubmitTime < submitTime){//取最新提交的一笔
    //					lastPaymentEntity = p;
    //					lastSubmitTime = submitTime;
    //				}
    //			}
    //			return lastPaymentEntity;
    //		}else{
    //			return null;
    //		}
    //	}

    @Override
    public PaymentEntity[] prepareSubmitting(PaymentEntity[] paymentEntitys) {
        Date now = new Date();
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List<PaymentEntity> preparedPayments = new ArrayList<PaymentEntity>();
            for (PaymentEntity paymentEntity : paymentEntitys) {
                Query updateQ = session.createQuery("update PaymentEntity " + "set status = :newStatus, updateTime=:updateTime " + "where id = :id and status = :oldStatus");
                updateQ.setInteger("newStatus", PaymentStatus.STATUS_TO_SUBMIT);//设置为准备提交
                updateQ.setTimestamp("updateTime", now);
                updateQ.setLong("id", paymentEntity.getId());
                updateQ.setInteger("oldStatus", PaymentStatus.STATUS_INIT);//旧的为初始化
                if (updateQ.executeUpdate() == 1) {
                    preparedPayments.add(paymentEntity);
                } else {
                    logger.info("支付交易【ID=" + paymentEntity.getId() + "】状态异常，可能已经撤销交易。");
                }
            }
            tx.commit();
            return preparedPayments.toArray(new PaymentEntity[0]);
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public void save(BatchPaymentEntity batchEntity) {
        List<PaymentEntity> payments = batchEntity.getPaymentEntitys();
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            //如果不设为空，hibernate会自动维护关系，再把PaymentEntity表中的batchSeqId再update一次
            batchEntity.setPaymentEntitys(null);
            int i = 0;
            for (PaymentEntity payment : payments) {
                session.save(payment);
                if (++i % 30 == 0) { //30, same as the JDBC batch size
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();
                }
            }
            session.save(batchEntity);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            batchEntity.setPaymentEntitys(payments);
            if (session != null) session.close();
        }
    }

    @Override
    public BatchPaymentEntity getBatchPaymentEntity(String batchSeqId) {
        Session session = getSession();//这里如果关闭session，延迟加载有问题
        return (BatchPaymentEntity) session.get(BatchPaymentEntity.class, batchSeqId);
    }
    
    @Override
    public PaymentEntity getPaymentsByBankSeqId(String bankSeqId){
    	 Session session = getSession();
    	 PaymentEntity paymentEntity=null;
         try {
             Query q = session.createQuery("from PaymentEntity where bankSeqId=:bankSeqId");
             q.setString("bankSeqId", bankSeqId);
             List<PaymentEntity> list= q.list();
             if(list!=null&&list.size()>0){
            	 paymentEntity=list.get(0);
             }
         } catch (Exception e){
        	 e.printStackTrace();
         }finally {
             session.close();
         }
		return paymentEntity;
    }

    @Override
    public void updateQueryTransCount(Long id) {
        Session session = getSession();
        try {
            Query q = getSession().createQuery("update PaymentEntity set queryTransCount = queryTransCount + 1 where id = :id ");
            q.setLong("id", id);
            q.executeUpdate();
        } finally {
            session.close();
        }
    }

    @Override
    public List<PaymentEntity> getPaymentsByIds(Long[] ids) {
        Session session = getSession();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < ids.length; i++) {
                if (i != 0) sb.append(",");
                sb.append("?");
            }
            sb.append(")");
            Query q = session.createQuery("from PaymentEntity where id in " + sb);
            for (int i = 0; i < ids.length; i++) {
                q.setLong(i, ids[i]);
            }
            return (List<PaymentEntity>) q.list();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateBatch(PaymentEntity[] payments) {
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            int i = 0;
            for (PaymentEntity payment : payments) {
                session.update(payment);
                if (++i % 30 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public List findPaymentByStatus(int status) {
        Session session = getSession();
        try {
            Query q = session.createQuery("select p.id, p.batchSeqId, p.bankBatchSeqId, p.bankName, p.accNo from PaymentEntity p where p.status = :status");
            q.setInteger("status", status);
            List list = q.list();
            return list;
        } finally {
            session.close();
        }

    }

    @Override
    public void updatePaymentStatus(PaymentEntity[] paymentEntitys, boolean isQuery) {
        Session session = getSession();
        Transaction tx = null;
        Date now = new Date();
        Query q;
        try {
            tx = session.beginTransaction();
            if (isQuery)
				q = session.createQuery("update PaymentEntity set " 
                        + " queryTransCount=:queryTransCount,"
						+ " status=:status, " 
                        + " statusMsg=:statusMsg, " 
						+ " payBankStatus=:payBankStatus, "
						+ " payBankStatusMsg=:payBankStatusMsg, " 
						+ " payErrorCode=:payErrorCode, "
						+ " bankStatus=:bankStatus, "
						+ " bankStatusMsg=:bankStatusMsg,"
						+ " callbackExtProperties=:callbackExtProperties,"
						+ " errorCode=:errorCode, " 
						+ " updateTime=:updateTime " 
						+ " where id=:id");
            else 
				q = session.createQuery("update PaymentEntity set " 
                        + " status=:status, " 
						+ " statusMsg=:statusMsg, "
						+ " payBankStatus=:payBankStatus, " 
						+ " payBankStatusMsg=:payBankStatusMsg, "
						+ " payErrorCode=:payErrorCode, " 
						+ " bankStatus=:bankStatus, "
						+ " bankStatusMsg=:bankStatusMsg, " 
						+ " errorCode=:errorCode, "
						+ " submitPayTime=:submitPayTime,"
						+ " callbackExtProperties=:callbackExtProperties,"
						+ " updateTime=:updateTime " 
						+ " where id=:id");
            int i = 0;
            for (PaymentEntity payment : paymentEntitys) {
                if (isQuery) {
                    q.setInteger("queryTransCount", payment.getQueryTransCount() + 1);
                    q.setInteger("status", payment.getStatus());
                    q.setString("statusMsg", payment.getStatusMsg());
                    q.setString("payBankStatus", payment.getPayBankStatus());
                    q.setString("payBankStatusMsg", payment.getPayBankStatusMsg());
                    q.setInteger("payErrorCode", payment.getPayErrorCode());
                    q.setString("bankStatus", payment.getBankStatus());
                    q.setString("bankStatusMsg", payment.getBankStatusMsg());
                    q.setInteger("errorCode", payment.getErrorCode());
                    q.setTimestamp("updateTime", now);
                    q.setString("callbackExtProperties", payment.getCallbackExtProperties());
                    q.setLong("id", payment.getId());
                    q.executeUpdate();
                } else {
                    q.setInteger("status", payment.getStatus());
                    q.setString("statusMsg", payment.getStatusMsg());
                    q.setString("payBankStatus", payment.getPayBankStatus());
                    q.setString("payBankStatusMsg", payment.getPayBankStatusMsg());
                    q.setInteger("payErrorCode", payment.getPayErrorCode());
                    q.setString("bankStatus", payment.getBankStatus());
                    q.setString("bankStatusMsg", payment.getBankStatusMsg());
                    q.setInteger("errorCode", payment.getErrorCode());
                    q.setTimestamp("submitPayTime", now);
                    q.setTimestamp("updateTime", now);
                    q.setString("callbackExtProperties", payment.getCallbackExtProperties());
                    q.setLong("id", payment.getId());
                    q.executeUpdate();
                }
                if (++i % 30 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }
    //add it by caolipeng start at 2015-08-18
	@Override
	public List<String> findPaymentByBatchSeqId(List<String> batchSeqIds, String accNo, Date beginDate, Date endDate) {
		Session session = getSession();
        try {
            StringBuilder sb = new StringBuilder();
            Date eDate = DateTimeUtil.addDay(endDate, 1);
            sb.append("(");
            for (int i = 0; i < batchSeqIds.size(); i++) {
                if (i != 0) sb.append(",");
                sb.append("?");
            }
            sb.append(")");
            Query q = session.createQuery("select distinct bankBatchSeqId from PaymentEntity where batchSeqId in " + sb
            								+" and accNo=:accNo "
            								+" and transDate>=:beginDate " + "and transDate<:endDate ");
            for (int i = 0; i < batchSeqIds.size(); i++) {
                q.setString(i, batchSeqIds.get(i));
            }
            q.setString("accNo", accNo);
			q.setDate("beginDate", beginDate);
			q.setDate("endDate", eDate);
			List<String> bankBatchSeqId = (List<String>) q.list();
            return bankBatchSeqId;
        } finally {
            session.close();
        }
	}

	@Override
	public List<String> findBatchSeqIdByAccAndDate(String accNo,Date beginDate, Date endDate) {
		Session session = getSession();
		try{
			Date eDate = DateTimeUtil.addDay(endDate, 1);//日期为2015-08-28 00:00:00
			Query q = session.createQuery("select batchSeqId from BatchPaymentEntity " + "where accNo=:accNo "+"and transDate>=:beginDate " + "and transDate<:endDate ");
			q.setString("accNo", accNo);
			q.setDate("beginDate", beginDate);
			q.setDate("endDate", eDate);
			List<String> batchSeqIds = (List<String>)q.list();
			return batchSeqIds;
		} finally {
            session.close();
        }
	}
	//add it by caolipeng end at 2015-08-18
}
