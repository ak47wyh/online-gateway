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
package com.iboxpay.settlement.gateway.common.dao;

import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;

/**
 * The class AccountVerifyDao.
 *
 * Description: 卡验证dao接口
 *
 * @author: weiyuanhua
 * @since: 2015年10月14日 上午11:06:14 	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public interface AccountVerifyDao extends BaseDao<AccountVerifyEntity> {
	
	
	/**
	 * 根据批次号获取验证卡信息
	 * @param sysName 来源
	 * @param seqId 流水明细号
	 * @return
	 */
	public AccountVerifyEntity findAccountVerifyBySeqId(String sysName,String seqId);
	/**
     * 获取验证结果
     * @param accNo
     * @param accName
     * @param identityCode
     * @return
     */
    public AccountVerifyEntity getAccountVerifyEntity(String sysName, String accNo, String accName, String certNo, String mobileNo);

    /**
     * 保存新记录
     * @param accountVerifyEntity
     * @return
     */
    public boolean saveAccountVerify(AccountVerifyEntity accountVerifyEntity);
    
    /**
     * 更新记录
     * @param accountVerifyEntity
     * @return
     */
    public boolean updateAccountVerify(AccountVerifyEntity accountVerifyEntity);

    /**
     * 批量更新
     * @param paymentEntitys
     */
    public void updateBatch(AccountVerifyEntity accountVerifyEntitys[]);
    
    /**
     * 更新验卡状态
     * @param account
     */
    public void updateStatus(AccountVerifyEntity account);

}
