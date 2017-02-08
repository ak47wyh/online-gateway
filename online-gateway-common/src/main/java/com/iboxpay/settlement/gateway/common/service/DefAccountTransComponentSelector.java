package com.iboxpay.settlement.gateway.common.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.AccountTransConfig;
import com.iboxpay.settlement.gateway.common.trans.IAccountTransComponentSelector;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

@Service("accountTransComponentSelector")
public class DefAccountTransComponentSelector implements IAccountTransComponentSelector{
    private final Logger LOGGER = LoggerFactory.getLogger(DefAccountTransComponentSelector.class);
    @Resource
    AccountTransConfigService accountTransConfigService;
    
    @Override
    public IBankTrans[] select(AccountEntity accountEntity, String bankName, Class<? extends IBankTrans> bankTransClass) {
        List<IBankTrans> bankTrans = new ArrayList<IBankTrans>();;
        if(accountTransConfigService != null){
            Map<Class<? extends IBankTrans>, AccountTransConfig[]>  accountTransConfigsMap = accountTransConfigService.getTransConfigsByAccount(accountEntity);
            if(accountTransConfigsMap != null){
                AccountTransConfig[] accountTransConfigs = accountTransConfigsMap.get(bankTransClass);
                if(accountTransConfigs != null){
                    for(AccountTransConfig accountTransConfig : accountTransConfigs){
                        if(accountTransConfig.isComponentEnabled() && accountTransConfig.isComponentExist()){
                            bankTrans.add(accountTransConfig.getBankTrans());
                        }
                    }
                }
            }
        }else{
            LOGGER.warn("accountTransConfigService is null!!!!");
        }
        return bankTrans.toArray(new IBankTrans[0]);
    }

}

	