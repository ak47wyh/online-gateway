package com.iboxpay.settlement.gateway.common.trans.refund;

import java.io.IOException;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

public interface IRefundPayment extends IBankTrans{
	/**
	 * 支付退款
	 * @param payments 支付信息
	 * @throws BaseTransException
	 * @throws IOException
	 */
    public void refund(PaymentEntity[] payments) throws BaseTransException, IOException;
}
