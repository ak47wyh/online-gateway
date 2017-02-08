package com.iboxpay.settlement.gateway.common.dao;

import java.util.List;

import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.AccountTransConfig;


public interface AccountTransConfigDao {

    public List<AccountTransConfig> findByAccount(AccountEntity accountEntity);
    
    public void deleteByAccount(AccountEntity accountEntity);

    public void saveAccountTransConfig(AccountEntity accountEntity, List<AccountTransConfig> accountTransConfigList);

//    public void enableAccountTransConfig(AccountEntity accountEntity, boolean enable);
    /**
     * 过期的组件删除
     * @param accountTransConfig
     */
    public void deleteAccountTransConfig(AccountTransConfig accountTransConfig);
    
}

