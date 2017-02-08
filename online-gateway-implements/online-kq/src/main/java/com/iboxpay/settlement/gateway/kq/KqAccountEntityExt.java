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
package com.iboxpay.settlement.gateway.kq;

import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;


/**
 * 帐号扩展信息
 * @author liaoxiongjian
 * @date 2015-12-11 10:50
 */
public class KqAccountEntityExt extends AccountEntity {

	private static final long serialVersionUID = 1L;

	private Property memberCode = new Property("memberCode", "商户号ID");
	private Property merchantAcctId = new Property("merchantAcctId", "收款帐号");
	private Property contractId = new Property("contractId", "合同号");

	public Property getMemberCode() {
		return memberCode;
	}

	public Property getMerchantAcctId() {
		return merchantAcctId;
	}

	public Property getContractId() {
		return contractId;
	}
	
	

}
