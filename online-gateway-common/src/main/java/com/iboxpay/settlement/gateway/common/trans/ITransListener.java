package com.iboxpay.settlement.gateway.common.trans;

import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;

/**
 * 交易相关的监听器
 * @author jianbo_chen
 */
public interface ITransListener {

    /**
     * 批次提交完毕
     * @param accountEntity
     * @param batchSeqId
     */
    public void onBatchPaymentSubmitComplete(AccountEntity accountEntity, String batchSeqId, PaymentEntity[] paymentEntitys);

    /**
     * 交易状态查询完毕时
     * @param accountEntity
     * @param batchSeqId
     */
    public void onPaymentQueryComplete(AccountEntity accountEntity, String batchSeqId, PaymentEntity[] paymentEntitys);
}
