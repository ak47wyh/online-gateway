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
package com.iboxpay.settlement.gateway.wft;

import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;

/**
 * 
 * Description: 商户信息扩展
 *
 * @author: liaoxiongjian
 * @since: 2016年1月23日 上午11:09:42 	
 * @version: 1.0
 *
 */
public class WftAccountEntityExt extends AccountEntity {

	private static final long serialVersionUID = 1L;
	/**
	 * 威富通交易密钥
	 */
	public Property key = new Property("key", "威富通交易密钥");

	/**
	 * 威富通商户号
	 */
	public Property mchId = new Property("mchId", "威富通商户号");

	public Property getKey() {
		return key;
	}

	public Property getMchId() {
		return mchId;
	}
	
}
