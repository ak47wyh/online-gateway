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
package com.iboxpay.settlement.gateway.jzdsf;

import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;

/**
 * 
 * 集中代收付系统直联
 *
 * @author: fengweichao
 *	
 * @2016年2月22日  @下午4:30:59
 */
@Service
public class BankProfile implements IBankProfile {
    
    @Override
    public String getBankBasePackage() {
        Class currentClass = this.getClass();
        return currentClass.getName().replaceAll(currentClass.getSimpleName()+"$", "");
    }

    @Override
    public String getBankFullName() {
        return "集中代收付";
    }
    
    @Override
    public String getBankName() {
        return "jzdsf";
    }

    @Override
    public Class<? extends FrontEndConfig> getFrontEndConfigClass() {
        return JZDSFFrontEndConfig.class;
    }
    
    @Override
    public Class<? extends AccountEntity> getExtAccountEntityClass() {
        return null;
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

	