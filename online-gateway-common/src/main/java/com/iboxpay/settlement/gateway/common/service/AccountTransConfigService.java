package com.iboxpay.settlement.gateway.common.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.cache.ICacheService;
import com.iboxpay.settlement.gateway.common.dao.AccountTransConfigDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.AccountTransConfig;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;
import com.iboxpay.settlement.gateway.common.trans.balance.IBalance;
import com.iboxpay.settlement.gateway.common.trans.detail.IDetail;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;

@Service
public class AccountTransConfigService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountTransConfigService.class);
    @Resource
    AccountService accountService;
    
    @Resource(name = "localCacheService")
    ICacheService cacheService;

    @Resource
    AccountTransConfigDao accountTransConfigDao;
    
    
    private String getCachedKey(AccountEntity accountEntity){
        return "AccountTransConfig-" + accountEntity.getAccNo();
    }
    public Map<Class<? extends IBankTrans>, AccountTransConfig[]> getTransConfigsByAccount(AccountEntity accountEntity){
        String key = getCachedKey(accountEntity);
        Map<Class<? extends IBankTrans>, AccountTransConfig[]> accountTransConfigsMap = (Map<Class<? extends IBankTrans>, AccountTransConfig[]>)cacheService.get(key);
        if(accountTransConfigsMap == null){
            Map<Class<? extends IBankTrans>, AccountTransConfig[]> _accountTransConfigsMap = new LinkedHashMap<Class<? extends IBankTrans>, AccountTransConfig[]>();
            List<AccountTransConfig> accountTransConfigs = accountTransConfigDao.findByAccount(accountEntity);
            //接口配置与运行环境接口对比
            List<AccountTransConfig> paymentAccountTransConfigs = freshTransComponentState(accountEntity, accountTransConfigs, IPayment.class);
            _accountTransConfigsMap.put(IPayment.class, paymentAccountTransConfigs == null ? new AccountTransConfig[0] : paymentAccountTransConfigs.toArray(new AccountTransConfig[0]));
            List<AccountTransConfig> balanceAccountTransConfigs = freshTransComponentState(accountEntity, accountTransConfigs, IBalance.class);
            _accountTransConfigsMap.put(IBalance.class, balanceAccountTransConfigs == null ? new AccountTransConfig[0] : balanceAccountTransConfigs.toArray(new AccountTransConfig[0]));
            List<AccountTransConfig> detailAccountTransConfigs = freshTransComponentState(accountEntity, accountTransConfigs, IDetail.class);
            _accountTransConfigsMap.put(IDetail.class, detailAccountTransConfigs == null ? new AccountTransConfig[0] : detailAccountTransConfigs.toArray(new AccountTransConfig[0]));
            accountTransConfigsMap = _accountTransConfigsMap;
            cacheService.set(key, accountTransConfigsMap);
        }
        return accountTransConfigsMap;
    }
    private List<AccountTransConfig> freshTransComponentState(AccountEntity accountEntity, List<AccountTransConfig> accountTransConfigs, Class<? extends IBankTrans> transType) {
        IBankTrans[] trans = BankTransComponentManager.getBankComponent(accountEntity.getBankName(), transType);
        List<IBankTrans> leftTransList = new ArrayList<IBankTrans>();
        leftTransList.addAll(Arrays.asList(trans));
        List<AccountTransConfig> _accountTransConfigs = new ArrayList<AccountTransConfig>();
        if(accountTransConfigs != null && accountTransConfigs.size() > 0){
            for(AccountTransConfig accountTransConfig : accountTransConfigs){
                if(!transType.getName().equals(accountTransConfig.getTransComponentType()))//type过滤
                    continue;
                
                for(IBankTrans bankTrans : trans){//检查接口是否还存在
                    if(bankTrans.getClass().getName().equals(accountTransConfig.getPk().getTransComponent())){
                        accountTransConfig.setComponentExist(true);//接口存在。默认为不存在，不用设置： 检查配置中还有，但实现已经【删除了的组件】
                        accountTransConfig.setBankTrans(bankTrans);
                        leftTransList.remove(bankTrans);
                        break;
                    }
                }
                _accountTransConfigs.add(accountTransConfig);
            }
        }
        if(leftTransList.size() > 0){//在配置中找不到的组件为【新组件】，或者说尚未配置的
            for(IBankTrans bankTrans : leftTransList){
                if(!transType.isAssignableFrom(bankTrans.getClass()))//type过滤
                    continue;

                AccountTransConfig accountTransConfig = new AccountTransConfig();
                accountTransConfig.setPk(new AccountTransConfig.Pk(accountEntity, bankTrans.getClass().getName()));
                accountTransConfig.setBankTrans(bankTrans);
                accountTransConfig.setComponentNew(true);
                accountTransConfig.setComponentExist(true);
                accountTransConfig.setComponentEnabled(false);
                _accountTransConfigs.add(accountTransConfig);
            }
        }
        return _accountTransConfigs;
    }
    
    public void deleteByAccount(AccountEntity accountEntity){
        accountTransConfigDao.deleteByAccount(accountEntity);
        String key = getCachedKey(accountEntity);
        cacheService.delete(key);
    }

    public void saveAccountTransConfig(AccountEntity accountEntity, List<AccountTransConfig> accountTransConfigList){
        accountTransConfigDao.saveAccountTransConfig(accountEntity, accountTransConfigList);
        String key = getCachedKey(accountEntity);
        cacheService.delete(key);
    }

    /**
     * 过期的组件删除
     * @param accountTransConfig
     */
    public void deleteAccountTransConfig(AccountTransConfig accountTransConfig){
        accountTransConfigDao.deleteAccountTransConfig(accountTransConfig);
        String key = getCachedKey(accountTransConfig.getPk().getAccount());
        cacheService.delete(key);
    }

}

	