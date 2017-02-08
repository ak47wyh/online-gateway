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
package com.iboxpay.settlement.gateway.common.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.dao.AccountVerifyDao;
import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;

/**
 * The class AccountVerifyService.
 *
 * Description: 卡验证service实现类
 *
 * @author: weiyuanhua
 * @since: 2015年10月14日 上午11:05:04 	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
@Service
public class AccountVerifyService {

    @Resource
    private AccountVerifyDao accountVerifyDao;

    public AccountVerifyEntity getAccountVerifyEntity(String sysName, String accNo, String accName, String identityCode, String mobileNo) {
    	return accountVerifyDao.getAccountVerifyEntity(sysName, accNo, accName, identityCode, mobileNo);
    }
    
    public boolean saveAccountVerify(AccountVerifyEntity accountVerifyEntity) {
    	return accountVerifyDao.saveAccountVerify(accountVerifyEntity);
    }
    
    public boolean updateAccountVerify(AccountVerifyEntity accountVerifyEntity) {
    	return accountVerifyDao.updateAccountVerify(accountVerifyEntity);
    }
    
    public void updateBatch(AccountVerifyEntity accountVerifyEntitys[]) {
    	 for (AccountVerifyEntity accountVerify : accountVerifyEntitys)
    		 accountVerifyDao.update(accountVerify);
    }
}
