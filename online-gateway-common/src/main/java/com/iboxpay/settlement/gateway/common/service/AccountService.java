package com.iboxpay.settlement.gateway.common.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.cache.ICacheService;
import com.iboxpay.settlement.gateway.common.dao.AccountDao;
import com.iboxpay.settlement.gateway.common.dao.AccountTransConfigDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;

@Service
public class AccountService {

    @Resource
    AccountDao accountDao;
    
    @Resource
    AccountTransConfigService accountTransConfigService;
    
    @Resource(name = "localCacheService")
    ICacheService cacheService;

    final static String BANK_DEFAULT_PREFIX = "bank_def_";

    public List<AccountEntity> listAccount() {
        List<AccountEntity> list = accountDao.find(null, null);
        return list;
    }

    public void deleteAccountEntity(AccountEntity accountEntity) {
        clearDefaultAccountIfNeed(accountEntity);
        accountDao.delete(accountEntity);
        cacheService.setWithType(accountEntity.getAccNo(), null, AccountEntity.class);
        accountTransConfigService.deleteByAccount(accountEntity);//失败就失败吧
    }

    private void clearDefaultAccountIfNeed(AccountEntity accountEntity) {
        AccountEntity defaultBankAccount = getAccountEntityByBank(accountEntity.getBankName());
        if (defaultBankAccount != null && accountEntity.getAccNo().equals(defaultBankAccount.getAccNo()) || accountEntity.isBankDefault()) {
            cacheService.set(BANK_DEFAULT_PREFIX + accountEntity.getBankName(), null);
            if (defaultBankAccount != null) cacheService.setWithType(defaultBankAccount.getAccNo(), null, AccountEntity.class);
        }
    }

    public void addAccountEntity(AccountEntity accountEntity) {
        clearDefaultAccountIfNeed(accountEntity);
        accountDao.save(accountEntity);
        cacheService.setWithType(accountEntity.getAccNo(), accountEntity, AccountEntity.class);
    }

    public void updateAccountEntity(AccountEntity accountEntity) {
        clearDefaultAccountIfNeed(accountEntity);
        accountDao.update(accountEntity);
        cacheService.setWithType(accountEntity.getAccNo(), accountEntity, AccountEntity.class);
    }

    public AccountEntity getAccountEntity(String accNo) {
        AccountEntity accountEntity = (AccountEntity) cacheService.getWithType(accNo, AccountEntity.class);
        if (accountEntity == null) {
            accountEntity = accountDao.get(accNo);
            if (accountEntity != null) cacheService.setWithType(accNo, accountEntity, AccountEntity.class);
        }
        return accountEntity;
    }

    //	private List<AccountEntity> getHardcodeAccountList(){
    //		List<AccountEntity> list = new ArrayList<AccountEntity>();
    //		//bocom
    //		AccountEntity accountEntity = new BocomAccountEntityExt();
    //		accountEntity.setAccNo("443066089018010084372");
    //		accountEntity.setAccName("深圳盒子支付信息技术有限公司");
    //		accountEntity.setBankName("bocom");
    //		((BocomAccountEntityExt)accountEntity).getBusiNo().setVal("4430007664");
    //		list.add(accountEntity);
    //		//ccb
    //		accountEntity = new AccountEntity();
    //		accountEntity.setAccNo("31001576613050033984");
    //		accountEntity.setAccName("上海乾盒信息技术有限公司");
    //		accountEntity.setBankName("ccb");
    //		list.add(accountEntity);
    //		//cmb
    //		accountEntity = new AccountEntity();
    //		accountEntity.setAccNo("121911512210908");
    //		accountEntity.setCnaps("308290003134");//联行号
    //		accountEntity.setAccName("上海乾盒信息技术有限公司");
    //		accountEntity.setBankName("cmb");
    //		list.add(accountEntity);
    ////		//cmb
    ////		accountEntity = new AccountEntity();
    ////		accountEntity.setAccNo("591902896710201");
    ////		accountEntity.setCnaps("308391026010");//联行号
    ////		accountEntity.setAccName("银企直连专用账户9");
    ////		accountEntity.setBankName("cmb");
    ////		list.add(accountEntity);
    //		
    //		return list;
    //	}
    public AccountEntity getAccountEntityByBank(String bank) {
        String key = BANK_DEFAULT_PREFIX + bank;
        AccountEntity accountEntity = (AccountEntity) cacheService.get(key);
        if (accountEntity == null) {
            List<AccountEntity> bankAccounts = accountDao.find("bankName=?", bank);
            if (bankAccounts == null || bankAccounts.size() == 0) return null;
            for (AccountEntity bankAccount : bankAccounts) {
                if (bankAccount.isBankDefault()) {
                    accountEntity = bankAccount;
                    break;
                }
            }
            if (accountEntity != null) cacheService.set(key, accountEntity);
        }
        return accountEntity;
    }
}
