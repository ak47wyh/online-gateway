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

import java.io.Serializable;

public class CollectRequestModel implements Serializable {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */

	private static final long serialVersionUID = 1L;

	private String appCode;
	private String batchSeqId;
	private String orderSerial;
	private Double amount;
	private String accountNo;
	private String accountName;
	private String accountType;
	private String unionNo;
	private String unionName;
	private String netPayNo;
	private String bankName;
	private String extProperties;

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	public String getBatchSeqId() {
		return batchSeqId;
	}

	public void setBatchSeqId(String batchSeqId) {
		this.batchSeqId = batchSeqId;
	}

	public String getOrderSerial() {
		return orderSerial;
	}

	public void setOrderSerial(String orderSerial) {
		this.orderSerial = orderSerial;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getUnionNo() {
		return unionNo;
	}

	public void setUnionNo(String unionNo) {
		this.unionNo = unionNo;
	}

	public String getUnionName() {
		return unionName;
	}

	public void setUnionName(String unionName) {
		this.unionName = unionName;
	}

	public String getNetPayNo() {
		return netPayNo;
	}

	public void setNetPayNo(String netPayNo) {
		this.netPayNo = netPayNo;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getExtProperties() {
		return extProperties;
	}

	public void setExtProperties(String extProperties) {
		this.extProperties = extProperties;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
