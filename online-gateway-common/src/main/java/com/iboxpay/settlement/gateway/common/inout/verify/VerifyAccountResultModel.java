package com.iboxpay.settlement.gateway.common.inout.verify;

import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;

public class VerifyAccountResultModel extends  CommonResultModel{
	private static final long serialVersionUID = 1L;
	private String batchSeqId;//批次流水号	
    private VerifyAccountResult [] data;
    
    
	public String getBatchSeqId() {
		return batchSeqId;
	}
	public void setBatchSeqId(String batchSeqId) {
		this.batchSeqId = batchSeqId;
	}
	public VerifyAccountResult[] getData() {
		return data;
	}
	public void setData(VerifyAccountResult[] data) {
		this.data = data;
	}
    
    
	
}
