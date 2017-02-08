package com.iboxpay.settlement.gateway.jz.service.verify;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.verify.IAccountVerify;
import com.iboxpay.settlement.gateway.jz.service.query.QueryPayment_500501;

@Service
public class AccountVerify implements IAccountVerify{
	private static Logger logger = LoggerFactory.getLogger(QueryPayment_500501.class);
	public static final String BANK_TRANS_CODE = "accountVerify";
	@Override
	public TransCode getTransCode() {
		// TODO Auto-generated method stub
		return TransCode.VERIFY;
	}

	@Override
	public String getBankTransCode() {
		
		return BANK_TRANS_CODE;
	}

	@Override
	public String getBankTransDesc() {
		
		return "账户卡验证";
	}

	@Override
	public void verfiy(AccountVerifyEntity account) throws BaseTransException, IOException {
		System.out.println("进入验证卡测试方法");
		
	}

}
