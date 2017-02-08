package com.iboxpay.settlement.gateway.common.trans.callback;

import java.io.IOException;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

public interface ICallBackPayment extends IBankTrans<PaymentEntity[]>{
    /**
     * 第三方异步回调
     * @param paymentInfos
     */
    public void doCallback(PaymentEntity payment,CommonRequestModel requestModel) throws BaseTransException, IOException;
}
