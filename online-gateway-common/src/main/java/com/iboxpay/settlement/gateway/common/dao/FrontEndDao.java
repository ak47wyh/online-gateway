package com.iboxpay.settlement.gateway.common.dao;

import java.io.Serializable;
import java.util.List;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;

public interface FrontEndDao {

    public void save(FrontEndConfig feConfig);

    public void update(FrontEndConfig feConfig);

    public FrontEndConfig get(Serializable id);

    public boolean delete(FrontEndConfig feConfig);

    public boolean delete(Serializable id);

    /**
     * 某个银行的所有前置机配置
     * @param bankName
     * @return
     */
    public List<FrontEndConfig> findByBankName(String bankName);

    public List<FrontEndConfig> loadAllFrontEndConfig();

}
