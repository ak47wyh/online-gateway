package com.iboxpay.settlement.gateway.jd.service.payment;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.callback.ICallBackPayment;
import com.iboxpay.settlement.gateway.common.trans.close.IClosePayment;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.jd.JdpayFrontEndConfig;
import com.iboxpay.settlement.gateway.jd.service.JdContrants;
import com.iboxpay.settlement.gateway.jd.service.callback.CallbackPayment_JdTrade;
import com.iboxpay.settlement.gateway.jd.service.query.QueryPayment_JdTrade;
import com.iboxpay.settlement.gateway.jd.service.refund.RefundPayment_JdTrade;
import com.iboxpay.settlement.gateway.jd.service.refund.query.QueryRefundPayment_JdTrade;
import com.iboxpay.settlement.gateway.jd.service.reverse.ReversePayment_JdTrade;

/**
 * 京东钱包支付入口程序
 * @author liaoxiongjian
 * @date 2016-03-23
 */
@Service
public class Payment_JdTrade implements IPayment {

    private static Logger logger = LoggerFactory.getLogger(Payment_JdTrade.class);
    public static final String BANK_TRANS_CODE = "jdPay";
    public static final String BANK_TRANS_DESC = "京东钱包";



    @Resource
    private Payment_JdNative  paymentJdNative;
    
    @Resource
    private Payment_JdMicropay paymentJdMicropay;

    @Override
    public TransCode getTransCode() {
        return TransCode.PAY;
    }

    @Override
    public String getBankTransCode() {
        return BANK_TRANS_CODE;
    }

    @Override
    public String getBankTransDesc() {
        return BANK_TRANS_DESC;
    }

    @Override
    public PaymentNavigation navigate() {
        return PaymentNavigation.create().setBatchSize(1)//单笔
                .setDiffBank(true)//跨行
                .setSameBank(true).setToPrivate(true).setToCompany(true)//对公,对私都支持
                .setType(PaymentNavigation.Type.online);
    }

    @Override
    public boolean navigateMatch(PaymentEntity payment) {
        return true;
    }

    @Override
    public String check(PaymentEntity[] payments) {
        return null;
    }

    @Override
    public void genBankBatchSeqId(PaymentEntity[] payments) {
        String bankBatchSeqId = Sequence.genSequence();
        for (PaymentEntity payment : payments) {
            payment.setBankBatchSeqId(bankBatchSeqId);
        }
    }

    @Override
    public void genBankSeqId(PaymentEntity[] payments) {
        for (int i = 0; i < payments.length; i++) {
            String bankSeqId = payments[i].getSeqId();
            payments[i].setBankSeqId(String.valueOf(bankSeqId));
        }
    }

    @Override
    public void pay(PaymentEntity[] payments) throws BaseTransException {
        PaymentEntity paymentEntity = payments[0];
        String appType = paymentEntity.getAppType();
        JdpayFrontEndConfig jdConfig = (JdpayFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        if (appType.equals(JdContrants.PAY_TYPE_NATIVE)) {//扫码支付
        	paymentJdNative.pay(payments, jdConfig);
        } else if (appType.equals(JdContrants.PAY_TYPE_MICROPAY)) {//刷卡支付
        	paymentJdMicropay.pay(payments, jdConfig);
        }

    }

    @Override
    public Class<? extends IQueryPayment> getQueryClass() {
        return QueryPayment_JdTrade.class;
    }

    @Override
    public Class<? extends IRefundPayment> getRefundClass() {
        return RefundPayment_JdTrade.class;
    }

    @Override
    public Class<? extends IRefundQueryPayment> getRefundQueryClass() {
        return QueryRefundPayment_JdTrade.class;
    }

    @Override
    public Class<? extends IReversePayment> getReverseClass() {
        return ReversePayment_JdTrade.class;
    }

    @Override
    public Class<? extends IClosePayment> getCloseClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends ICallBackPayment> getCallBackClass() {
        return CallbackPayment_JdTrade.class;
    }
}
