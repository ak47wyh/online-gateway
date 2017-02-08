package com.iboxpay.settlement.gateway.common.trans.payment;

import java.util.LinkedList;
import java.util.List;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;

//经过银行接口分批后的信息
public class BankBatchPayment {

    private IPayment paymentImpl;
    private List<PaymentEntity> bankBatchPaymentEntitys;

    public BankBatchPayment(IPayment paymentImpl) {
        this.paymentImpl = paymentImpl;
    }

    public void setBankBatchPaymentEntitys(List<PaymentEntity> bankBatchPaymentEntitys) {
        this.bankBatchPaymentEntitys = bankBatchPaymentEntitys;
    }

    public void addBatch(PaymentEntity paymentEntity) {
        if (bankBatchPaymentEntitys == null) bankBatchPaymentEntitys = new LinkedList<PaymentEntity>();

        bankBatchPaymentEntitys.add(paymentEntity);
    }

    public IPayment getPaymentImpl() {
        return paymentImpl;
    }

    public List<PaymentEntity> getBankBatchPaymentEntitys() {
        return bankBatchPaymentEntitys;
    }

    public int size() {
        if (bankBatchPaymentEntitys == null)
            return 0;
        else return bankBatchPaymentEntitys.size();
    }
}
