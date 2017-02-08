/*
 * Copyright (C) 2011-2015 ShenZhen iBOXPAY Information Technology Co.,Ltd.
 * 
 * All right reserved.
 * 
 * This software is the confidential and proprietary
 * information of iBoxPay Company of China. 
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement 
 * you entered into with iBoxpay inc.
 *
 */
package com.iboxpay.settlement.gateway.common.dao.impl;

import java.util.ArrayList;
import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.dao.AccountVerifyDao;
import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * The class AccountVerifyDaoImpl.
 *
 * Description: 卡验证dao实现类
 *
 * @author: weiyuanhua
 * @since: 2015年10月14日 上午11:05:36 	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
@Component("accountVerifyDao")
public class AccountVerifyDaoImpl extends BaseDaoImpl<AccountVerifyEntity> implements AccountVerifyDao {

	@Override
	public AccountVerifyEntity getAccountVerifyEntity(String sysName, String accNo, String accName, String certNo, String mobileNo) {
		StringBuffer hql_sb = new StringBuffer("from AccountVerifyEntity where 1=1");
		ArrayList<Object> paramList = new ArrayList<Object>();
		if(StringUtils.isNotBlank(sysName)) {
			hql_sb.append(" and sysName = ?");
			paramList.add(sysName);
		}
		if(StringUtils.isNotBlank(accNo)) {
			hql_sb.append(" and customerAccNo = ?");
			paramList.add(accNo);
		}
		if(StringUtils.isNotBlank(accName)) {
			hql_sb.append(" and customerAccName = ?");
			paramList.add(accName);
		}
		if(StringUtils.isNotBlank(certNo)) {
			hql_sb.append(" and certNo = ?");
			paramList.add(certNo);
		}
		if(StringUtils.isNotBlank(mobileNo)) {
			hql_sb.append(" and mobileNo = ?");
			paramList.add(mobileNo);
		}
				
		return this.findEntityByHQL(hql_sb.toString(), paramList.toArray());
	}
	
	
	public AccountVerifyEntity findAccountVerifyBySeqId(String sysName,String seqId){
		String hql = "from AccountVerifyEntity where sysName=? and seqId=?";
		return this.findEntityByHQL(hql, new String[]{sysName, seqId});
	}

    @Override
    public boolean saveAccountVerify(AccountVerifyEntity accountVerifyEntity) {
    	boolean flag = false;
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(accountVerifyEntity);
            tx.commit();
            
            flag = true;
        } catch (RuntimeException e) {
        	flag = false;
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
        
        return flag;
    }
    
    @Override
    public boolean updateAccountVerify(AccountVerifyEntity accountVerifyEntity) {
    	boolean flag = false;
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(accountVerifyEntity);
            tx.commit();
            
            flag = true;
        } catch (RuntimeException e) {
        	flag = false;
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
        
        return flag;
    }

    @Override
    public void updateBatch(AccountVerifyEntity accountVerifyEntitys[]) {
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            int i = 0;
            for (AccountVerifyEntity entity : accountVerifyEntitys) {
                session.update(entity);
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
    
    
    public void updateStatus(AccountVerifyEntity account){
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            String hql="update AccountVerifyEntity set status=:status,"
            		 + " statusMsg=:statusMsg,"
            		 + " errorCode=:errorCode,"
            		 + " errorMsg =:errorMsg,"
            		 + " bankStatus =:bankStatus,"
            		 + " verifyErrorCode =:verifyErrorCode,"
            		 + " seqId =:seqId,"
            		 + " bankSeqId =:bankSeqId,"
            		 + " updateTime =:updateTime"
            		 + " where id=:id";
            
            Query query=session.createQuery(hql);
            query.setInteger("status", account.getStatus());
            query.setString("statusMsg", account.getStatusMsg());
            query.setString("errorCode", account.getErrorCode());
            query.setString("errorMsg", account.getErrorMsg());
            query.setString("bankStatus", account.getBankStatus());
            query.setString("verifyErrorCode", account.getVerifyErrorCode());
            query.setString("seqId", account.getSeqId());
            query.setString("bankSeqId", account.getBankSeqId());
            query.setDate("updateTime", new Date());
            query.setLong("id", account.getId());
            query.executeUpdate();
            		
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }
}
