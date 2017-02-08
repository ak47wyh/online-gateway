package com.iboxpay.settlement.gateway.common.trans.refund.query;

import java.io.IOException;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

public interface IRefundQueryPayment extends IBankTrans{
	/**
	 * 查询退款
	 * @param payments 支付信息
	 * @throws BaseTransException
	 * @throws IOException
	 */
    public void query(PaymentEntity[] payments) throws BaseTransException, IOException;
}
