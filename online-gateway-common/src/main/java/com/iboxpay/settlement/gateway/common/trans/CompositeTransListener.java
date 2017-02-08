package com.iboxpay.settlement.gateway.common.trans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.CompositeListener;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;

public class CompositeTransListener extends CompositeListener<ITransListener> implements ITransListener {

    static Logger logger = LoggerFactory.getLogger(CompositeTransListener.class);

    @Override
    public void onBatchPaymentSubmitComplete(AccountEntity accountEntity, String batchSeqId, PaymentEntity[] paymentEntitys) {

        Object[] listeners = getListeners();

        if (listeners != null) {
            for (int i = 0; i < listeners.length; i++) {
                try {
                    ITransListener listener = (ITransListener) listeners[i];
                    if (listener != null) listener.onBatchPaymentSubmitComplete(accountEntity, batchSeqId, paymentEntitys);
                } catch (Throwable re) {
                    logger.warn("exception received from listener " + listeners[i] + " when dispatching event", re);
                }
            }
        }

    }

    @Override
    public void onPaymentQueryComplete(AccountEntity accountEntity, String batchSeqId, PaymentEntity[] paymentEntitys) {

        Object[] listeners = getListeners();

        if (listeners != null) {
            for (int i = 0; i < listeners.length; i++) {
                try {
                    ITransListener listener = (ITransListener) listeners[i];
                    if (listener != null) listener.onPaymentQueryComplete(accountEntity, batchSeqId, paymentEntitys);
                } catch (Throwable re) {
                    logger.warn("exception received from listener " + listeners[i] + " when dispatching event", re);
                }
            }
        }
    }

}
