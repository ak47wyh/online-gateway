/*
 * Copyright (C) 2011-2015 ShenZhen iBOXPAY Information Technology Co.,Ltd.
 * 
 * All right reserved.
 * 
 * This software is the confidential and proprietary
 * information of iBoxPay Company of China. 
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement 
 * you entered into with iBoxpay inc.
 *
 */
package com.iboxpay.settlement.gateway.alipay;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
/**
 * 支付宝
 * @author liaoxiongjian
 * @date 2016年1月27日 下午1:59:48
 * @Version 1.0
 */
@Service
public class BankProfile implements IBankProfile{

	@Override
	public String getBankBasePackage() {
		Class currentClass = this.getClass();
		return currentClass.getName().replaceAll(currentClass.getSimpleName()+"$", "");
	}

	@Override
	public String getBankFullName() {
		return "支付宝";
	}

	@Override
	public String getBankName() {
		return "alipay";
	}

	@Override
	public Class<? extends AccountEntity> getExtAccountEntityClass() {
		return null;
	}

	@Override
	public Class<? extends FrontEndConfig> getFrontEndConfigClass() {
		return AlipayFrontEndConfig.class;
	}

	@Override
    public String convertToBankCurrency(String isoCurrency) {
		return null;
    }

    @Override
    public String convertToIsoCurrency(String bankCurrency) {
    	return null;
    }

}
