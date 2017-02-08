package com.iboxpay.settlement.gateway.common.dialect;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.trans.ITransListener;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentDelegateService;
import com.iboxpay.settlement.gateway.common.trans.query.QueryDelegateService;

//银行信息转换器
@Service
public class DialectErrorCodeTranslator implements ITransListener {

    @PostConstruct
    private void init() {
        PaymentDelegateService.getCompositelistener().addListener(this);//支付时使用
        QueryDelegateService.getCompositelistener().addListener(this);//查询时使用
    }
    //支付交易调用后触发 
    @Override
    public void onBatchPaymentSubmitComplete(AccountEntity accountEntity, String batchSeqId, PaymentEntity[] paymentEntitys) {
        // TODO
    }
    //查询交易调用后触发
    @Override
    public void onPaymentQueryComplete(AccountEntity accountEntity, String batchSeqId, PaymentEntity[] paymentEntitys) {
        // TODO
    }
}
