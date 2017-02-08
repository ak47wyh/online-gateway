package com.iboxpay.settlement.gateway.common.dao.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iboxpay.settlement.gateway.common.dao.AccountTransConfigDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.AccountTransConfig;

/**
 * 
 * @author: jianbo_chen
 * @since: 2015年5月21日
 * @version:
 */
@Component("accountTransConfigDao")
public class AccountTransConfigDaoImpl extends BaseDaoImpl<AccountTransConfig> implements AccountTransConfigDao{
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountTransConfigDaoImpl.class);
    
    @Transactional
    public List<AccountTransConfig> findByAccount(AccountEntity accountEntity) {
        return findByHQL("from AccountTransConfig where pk.account = ? order by transOrder desc", accountEntity);
    }

    @Transactional
    public void deleteByAccount(AccountEntity accountEntity) {
        deleteByAccount0(accountEntity);
    }
    
    @Transactional
    public void saveAccountTransConfig(AccountEntity accountEntity, List<AccountTransConfig> accountTransConfigList) {
        deleteByAccount0(accountEntity);
        Date now = new Date();
        for(AccountTransConfig accountTransConfig : accountTransConfigList){
            accountTransConfig.setCreateTime(now);
            accountTransConfig.setUpdateTime(now);
            save(accountTransConfig);
        }
        LOGGER.info("保存账号 {} 定制接口配置共 {} 条", accountEntity.getAccNo(), accountTransConfigList.size());
    }

    private void deleteByAccount0(AccountEntity accountEntity) {
        Query deleteQ = createQuery("delete from AccountTransConfig where pk.account = ? ", accountEntity);
        int oldCount = deleteQ.executeUpdate();
        LOGGER.info("删除账号 {} 定制接口配置共 {} 条", accountEntity.getAccNo(), oldCount);
    }
    
    @Transactional
    public void deleteAccountTransConfig(AccountTransConfig accountTransConfig) {
        delete(accountTransConfig);
        LOGGER.info("删除账号 {} 定制接口 ", accountTransConfig.getPk().getAccount().getAccNo(), accountTransConfig.getPk().getTransComponent());
    }

}

	