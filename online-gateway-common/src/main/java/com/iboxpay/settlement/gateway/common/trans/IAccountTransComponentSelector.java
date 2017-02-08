package com.iboxpay.settlement.gateway.common.trans;

import com.iboxpay.settlement.gateway.common.domain.AccountEntity;

/**
 * 账号交易组件选择器。默认情况下都走银行级实现的接口顺序，在AccountEntity的transConfigEnable开启的情况下，会走账号配置的。
 * @author: jianbo_chen
 * @since: 2015年5月29日
 * @version:
 */
public interface IAccountTransComponentSelector {
    public IBankTrans[] select(AccountEntity accountEntity, String bankName, Class<? extends IBankTrans> bankTransClass);
}

	