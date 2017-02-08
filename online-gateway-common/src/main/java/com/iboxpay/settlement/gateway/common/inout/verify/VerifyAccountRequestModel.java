package com.iboxpay.settlement.gateway.common.inout.verify;

import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;

public class VerifyAccountRequestModel extends CommonRequestModel{
	
	private static final long serialVersionUID = 1L;
    private String batchSeqId;//批次流水号	
    private VerifyAccountInfo [] data;
    
	public String getBatchSeqId() {
		return batchSeqId;
	}
	public void setBatchSeqId(String batchSeqId) {
		this.batchSeqId = batchSeqId;
	}
	public VerifyAccountInfo[] getData() {
		return data;
	}
	public void setData(VerifyAccountInfo[] data) {
		this.data = data;
	}
	
}
