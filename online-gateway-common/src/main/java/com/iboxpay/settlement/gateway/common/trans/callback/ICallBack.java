package com.iboxpay.settlement.gateway.common.trans.callback;

import java.io.IOException;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

public interface ICallBack extends IBankTrans<PaymentEntity[]>{
    /**
     * 第三方异步回调
     * @param paymentInfos
     */
    public void callback(PaymentEntity[] payments) throws BaseTransException, IOException;
}
