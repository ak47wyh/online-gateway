package com.iboxpay.settlement.gateway.common.trans.check;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import com.iboxpay.settlement.gateway.common.dao.PaymentCheckRecordDao;
import com.iboxpay.settlement.gateway.common.dao.PaymentCheckResultDao;
import com.iboxpay.settlement.gateway.common.domain.PaymentCheckResultEntity;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

/**
 * 对账策略
 * @author jianbo_chen
 */
public interface IPaymentChecker extends IBankTrans {

    public List<PaymentCheckResultEntity> check(String accNo, Date transDate, Session session, PaymentCheckResultDao paymentCheckResultDao, PaymentCheckRecordDao paymentCheckRecordDao)
            throws Exception;
}
