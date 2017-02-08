package com.iboxpay.settlement.gateway.common.inout.query;

import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;

public class QueryPaymentRequestModel extends CommonRequestModel {

    private static final long serialVersionUID = 1L;

    private String batchSeqId;//批次流水号	
    private boolean forceRefresh;//强制刷新状态(到银行取最新的状态)	

    public String getBatchSeqId() {
        return batchSeqId;
    }

    public void setBatchSeqId(String batchSeqId) {
        this.batchSeqId = batchSeqId;
    }

    public boolean isForceRefresh() {
        return forceRefresh;
    }

    public void setForceRefresh(boolean forceRefresh) {
        this.forceRefresh = forceRefresh;
    }
}
