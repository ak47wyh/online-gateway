package com.iboxpay.settlement.gateway.common.trans.verify;

import java.io.IOException;

import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

public interface IAccountVerify extends IBankTrans{
	/**
	 * 冲正
	 * @param payments 支付信息
	 * @throws BaseTransException
	 * @throws IOException
	 */
    public void verfiy(AccountVerifyEntity account) throws BaseTransException, IOException;
}
