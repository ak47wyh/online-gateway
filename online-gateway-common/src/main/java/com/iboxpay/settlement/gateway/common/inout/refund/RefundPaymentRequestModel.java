package com.iboxpay.settlement.gateway.common.inout.refund;

import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;

public class RefundPaymentRequestModel extends CommonRequestModel {

	private static final long serialVersionUID = 1L;
	private String batchSeqId;// 批次流水号
	private RefundPaymentCustomerInfo[] data;//	支付信息 支持批量支付
	
	public String getBatchSeqId() {
		return batchSeqId;
	}

	public void setBatchSeqId(String batchSeqId) {
		this.batchSeqId = batchSeqId;
	}

	public RefundPaymentCustomerInfo[] getData() {
		return data;
	}

	public void setData(RefundPaymentCustomerInfo[] data) {
		this.data = data;
	}
	

}
