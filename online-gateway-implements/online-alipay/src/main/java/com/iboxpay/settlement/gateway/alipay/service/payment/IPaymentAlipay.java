package com.iboxpay.settlement.gateway.alipay.service.payment;

import com.iboxpay.settlement.gateway.alipay.AlipayFrontEndConfig;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;

public interface IPaymentAlipay {
	public void pay(PaymentEntity[] payments,AlipayFrontEndConfig alipayConfig) throws BaseTransException;
}
