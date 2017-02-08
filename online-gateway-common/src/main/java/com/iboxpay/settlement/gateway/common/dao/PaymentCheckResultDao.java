package com.iboxpay.settlement.gateway.common.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.iboxpay.settlement.gateway.common.domain.PaymentCheckRecordEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentCheckResultEntity;
import com.iboxpay.settlement.gateway.common.page.PageBean;

public interface PaymentCheckResultDao extends BaseDao<PaymentCheckRecordEntity> {

    public void save(String accNo, Date checkDate, List<PaymentCheckResultEntity> checkResultList);

    public PageBean queryCheckResults(String accNo, String customerAccNo, String customerAccName, BigDecimal beginAmount, BigDecimal endAmount, Date transDate, String hasCheck, int status,
            int pageSize, int pageNo);
}
