package com.iboxpay.settlement.gateway.common.dao;

import java.util.Date;

import com.iboxpay.settlement.gateway.common.domain.PaymentCheckRecordEntity;

public interface PaymentCheckRecordDao extends BaseDao<PaymentCheckRecordEntity> {

    /**
     * 设置已经对账
     * @param accNo
     * @param checkDate
     */
    public void setCheck(String accNo, Date checkDate);

    /**
     * 某个账号某一天是否已经对过账
     * @param accNo
     * @param checkDate
     * @return
     */
    public boolean hasCheck(String accNo, Date checkDate);

    /**
     * 读取对账记录
     * @param accNo
     * @param checkDate
     * @return
     */
    public PaymentCheckRecordEntity getCheckRecord(String accNo, Date checkDate);

}
