package com.iboxpay.settlement.gateway.common;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;

/**
 * 银行实现的入口，主要信息概览，每个银行实现必须实现。
 * @author jianbo_chen
 */
public interface IBankProfile {

    /**银行简称*/
    public String getBankName();

    /**银行全称*/
    public String getBankFullName();

    /**前置机配置类*/
    public Class<? extends FrontEndConfig> getFrontEndConfigClass();

    /**ISO币别转为银行币别 */
    public String convertToBankCurrency(String isoCurrency);

    /**银行币别转为ISO币别*/
    public String convertToIsoCurrency(String bankCurrency);

    /**每家银行唯一的包路径,必须唯一.用于区别组件所属的银行.*/
    public String getBankBasePackage();

    /**
     * 扩展的账号类。用于主账号（发起交易的账号）的属性扩展。如：交通银行的账号有个“代发协议编号”需要配置
     * @return 没有扩展账号属性的，直接返回null。
     */
    public Class<? extends AccountEntity> getExtAccountEntityClass();
}
