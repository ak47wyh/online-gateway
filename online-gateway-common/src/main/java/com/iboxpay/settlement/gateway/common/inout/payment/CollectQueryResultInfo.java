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
package com.iboxpay.settlement.gateway.common.inout.payment;

import java.util.List;

import com.iboxpay.settlement.gateway.common.inout.CollectInfo;
import com.iboxpay.settlement.gateway.common.inout.CollectResultModel;

public class CollectQueryResultInfo extends CollectResultModel {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
		
	private static final long serialVersionUID = 1L;
	private List<CollectInfo> data;

	public List<CollectInfo> getData() {
		return data;
	}

	public void setData(List<CollectInfo> data) {
		this.data = data;
	}
}
