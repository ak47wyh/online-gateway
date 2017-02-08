package com.iboxpay.settlement.gateway.common.trans.reverse;

import java.io.IOException;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

public interface IReversePayment extends IBankTrans{
	/**
	 * 冲正
	 * @param payments 支付信息
	 * @throws BaseTransException
	 * @throws IOException
	 */
    public void reverse(PaymentEntity[] payments) throws BaseTransException, IOException;
}
