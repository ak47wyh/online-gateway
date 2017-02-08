package com.iboxpay.settlement.gateway.jd.service.payment;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.jd.JdpayFrontEndConfig;

public interface IPaymentJdTrade {
	public void pay(PaymentEntity[] payments,JdpayFrontEndConfig jdConfig) throws BaseTransException;
}
