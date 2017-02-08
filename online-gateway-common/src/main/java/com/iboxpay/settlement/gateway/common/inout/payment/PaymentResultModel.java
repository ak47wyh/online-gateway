package com.iboxpay.settlement.gateway.common.inout.payment;

import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;

/**
 * 支付结果
 * @author jianbo_chen
 */
public class PaymentResultModel extends CommonResultModel {

    private static final long serialVersionUID = 1L;

    private String batchSeqId;//批次流水号	
    private PaymentCustomerResult[] data;

    public String getBatchSeqId() {
        return batchSeqId;
    }

    public void setBatchSeqId(String batchSeqId) {
        this.batchSeqId = batchSeqId;
    }

    public PaymentCustomerResult[] getData() {
        return data;
    }

    public void setData(PaymentCustomerResult[] data) {
        this.data = data;
    }

}
