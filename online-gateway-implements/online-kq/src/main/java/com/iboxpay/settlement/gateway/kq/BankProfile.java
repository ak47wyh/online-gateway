package com.iboxpay.settlement.gateway.kq;

import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;

@Service
public class BankProfile implements IBankProfile{

	@Override
	public String getBankName() {
		return "kq";
	}

	@Override
	public String getBankFullName() {
		return "快钱代扣";
	}

	@Override
	public Class<? extends FrontEndConfig> getFrontEndConfigClass() {
		 return KQFrontEndConfig.class;
	}

	@Override
	public String convertToBankCurrency(String isoCurrency) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String convertToIsoCurrency(String bankCurrency) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBankBasePackage() {
		Class currentClass = this.getClass();
        return currentClass.getName().replaceAll(currentClass.getSimpleName() + "$", "");
	}

	@Override
	public Class<? extends AccountEntity> getExtAccountEntityClass() {
		return KqAccountEntityExt.class;
	}

}
