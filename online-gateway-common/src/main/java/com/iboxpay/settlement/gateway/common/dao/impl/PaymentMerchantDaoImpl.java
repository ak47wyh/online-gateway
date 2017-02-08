package com.iboxpay.settlement.gateway.common.dao.impl;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import com.iboxpay.settlement.gateway.common.dao.PaymentMerchantDao;
import com.iboxpay.settlement.gateway.common.domain.PaymentMerchantEntity;
import com.iboxpay.settlement.gateway.common.page.PageBean;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Component("paymentMerchantDao")
public class PaymentMerchantDaoImpl extends HibernateDaoSupport implements PaymentMerchantDao{

	
	public PaymentMerchantDaoImpl() {

	}

	@Resource
	public void setMySessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	/**
	 * 
	 */
    public PaymentMerchantEntity findByAppCode(String appCode,String payMerchantSubNo){
        Session session = getSession();
        PaymentMerchantEntity paymentMerchantEntity=null;
        try {
            Query q = session.createQuery("from PaymentMerchantEntity where appCode=:appCode and payMerchantSubNo=:payMerchantSubNo");
            q.setString("appCode", appCode);
            q.setString("payMerchantSubNo", payMerchantSubNo);
            List<PaymentMerchantEntity> list=q.list();
            if(list!=null&&list.size()>0){
            	paymentMerchantEntity=list.get(0);
            }
            return paymentMerchantEntity;
        } finally {
            session.close();
        }
        
    }
    


    public PageBean findPage(int pageNo, int pageSize,Map<String,Object> paramMap) {
        Session session = getSession();
        Transaction tx = null;
        try {
            session.setFlushMode(FlushMode.MANUAL);//使用oracle read-only 事务
            tx = session.beginTransaction();
     
            // 查询分页条数
            String sql="from PaymentMerchantEntity where 1=1";
            List<Object> params = new LinkedList<Object>();
            String appCode = (String) paramMap.get("appCode");
            if (!StringUtils.isBlank(appCode)) {
            	sql+=" and appCode=:appCode ";
            }
            String payMerchantNo = (String) paramMap.get("payMerchantNo");
            if (!StringUtils.isBlank(payMerchantNo)) {
                sql+=" and payMerchantNo=:payMerchantNo ";
            }
            
            String hql="select count(*) "+sql;
            Query query = session.createQuery(hql);
            if (!StringUtils.isBlank(appCode)) {
                query.setParameter("appCode", appCode);
            }
            if (!StringUtils.isBlank(payMerchantNo)) {
                query.setParameter("payMerchantNo", payMerchantNo);
            }
            Long totalCount = (Long) query.uniqueResult();
            
            
            // 查询账号分页信息
            PageBean pageBean = new PageBean(pageNo, pageSize, totalCount);
            Query queryList = session.createQuery(sql);
            if (!StringUtils.isBlank(appCode)) {
            	queryList.setParameter("appCode", appCode);
            }
            if (!StringUtils.isBlank(payMerchantNo)) {
            	queryList.setParameter("payMerchantNo", payMerchantNo);
            }
            queryList.setFirstResult(pageBean.getStartIndex());
            queryList.setMaxResults(pageBean.getPageSize());
            List result = queryList.list();
            pageBean.setResult(result);

            tx.commit();
            return pageBean;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
    

	@Override
	public void save(PaymentMerchantEntity entity) {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(entity);
			logger.info("save AccountEntity(" + entity.getClass().getName() + "): " + entity.toString());
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	@Override
	public void update(PaymentMerchantEntity entity) {
		Session session = getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(entity);
			logger.info("update AccountEntity(" + entity.getClass().getName() + "): " + entity.toString());
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();
			throw e;
		} finally {
			session.close();
		}
	}
    
	
	public PaymentMerchantEntity load(Long id){
		Session session = getSession();
		try {
			PaymentMerchantEntity paymentmerchant = (PaymentMerchantEntity) session.get(PaymentMerchantEntity.class,id);
			return paymentmerchant;
		} finally {
			session.close();
		}
	}
	
	
	
	public Boolean delete(Long id){
		Session session = getSession();
		Transaction tx = null;
		Boolean flag=false;
		try {
			tx = session.beginTransaction();
			PaymentMerchantEntity paymentmerchant = (PaymentMerchantEntity) session.get(PaymentMerchantEntity.class,id);
			session.delete(paymentmerchant);
			tx.commit();
			flag=true;
		} finally {
			session.close();
		}
		return flag;
	}
	
	

}

