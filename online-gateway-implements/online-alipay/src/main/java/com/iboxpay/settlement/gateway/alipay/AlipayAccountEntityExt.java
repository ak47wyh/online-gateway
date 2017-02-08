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
package com.iboxpay.settlement.gateway.alipay;

import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;

/**
 * 
 * Description: 商户信息扩展
 *
 * @author: liaoxiongjian
 * @since: 2016年2月22日 上午11:09:42 	
 * @version: 1.0
 *
 */
public class AlipayAccountEntityExt extends AccountEntity {

	private static final long serialVersionUID = 1L;
    /**
     * 威富通交易密钥
     */
    private  Property key = new Property("key", "交易密钥");
    
    /**
     * 支付宝合作方ID
     */
    private Property partnerId= new Property("partnerId", "支付宝合作方ID");
    
    /**
     * 支付宝代理商ID
     */
    private Property agentId= new Property("agentId", "支付宝代理商ID");


	public Property getPartnerId() {
		return partnerId;
	}

	public Property getAgentId() {
		return agentId;
	}

	public Property getKey() {
		return key;
	}

}
