package com.iboxpay.settlement.gateway.xmcmbc;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
/**
 * 民生银行厦门分行
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
@Service
public class BankProfile implements IBankProfile{

	@Override
	public String getBankName() {
		return "xmcmbc";
	}

	@Override
	public String getBankFullName() {
		return "民生银行厦门分行";
	}

	@Override
	public Class<? extends FrontEndConfig> getFrontEndConfigClass() {
		return XmcmbcFrontEndConfig.class;
	}

	@Override
	public String convertToBankCurrency(String isoCurrency) {
		return null;
	}

	@Override
	public String convertToIsoCurrency(String bankCurrency) {
		return null;
	}

	@Override
	public String getBankBasePackage() {
		Class currentClass = this.getClass();
        return currentClass.getName().replaceAll(currentClass.getSimpleName() + "$", "");
	}

	@Override
	public Class<? extends AccountEntity> getExtAccountEntityClass() {
		return null;
	}

}
