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
package com.iboxpay.settlement.gateway.jz;

import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;

/**
 * The class JzAccountEntityExt.
 *
 * Description: 商户信息扩展
 *
 * @author: weiyuanhua
 * @since: 2015年10月14日 上午11:09:42 	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public class JzAccountEntityExt extends AccountEntity {

	private static final long serialVersionUID = 1L;

	private Property merchantId = new Property("MERCHANT_ID", "商户号ID(余额查询绑定使用)");
	private Property acctType = new Property("ACC_TYPE", "账户类别(01-基本账户,02-手续费账户)");

	public Property getAcctType() {
		return acctType;
	}

	public Property getMerchantId() {
		return merchantId;
	}
}
