package com.iboxpay.settlement.gateway.common.trans.close;

import java.io.IOException;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

public interface IClosePayment extends IBankTrans{
	/**
	 * 支付退款
	 * @param payments 支付信息
	 * @throws BaseTransException
	 * @throws IOException
	 */
    public void closeOrder(PaymentEntity[] payments) throws BaseTransException, IOException;

}
