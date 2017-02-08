package com.iboxpay.settlement.gateway.common.trans.query;

import java.io.IOException;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

/**
 * 查询支付状态接口
 * @author jianbo_chen
 */
public interface IQueryPayment extends IBankTrans<PaymentEntity[]> {

    /**
     * 查询状态
     * @param paymentInfos
     */
    public void query(PaymentEntity[] payments) throws BaseTransException, IOException;
}
