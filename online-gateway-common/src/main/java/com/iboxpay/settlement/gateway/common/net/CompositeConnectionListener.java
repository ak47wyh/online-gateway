package com.iboxpay.settlement.gateway.common.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.CompositeListener;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.exception.FrontEndException;

public class CompositeConnectionListener extends CompositeListener<IConnectionListener> implements IConnectionListener {

    private static Logger logger = LoggerFactory.getLogger(CompositeConnectionListener.class);

    @Override
    public void onConnectionFail(String ip, int port, FrontEndConfig feConfig, FrontEndException e) {
        Object[] listeners = getListeners();

        if (listeners != null) {
            for (int i = 0; i < listeners.length; i++) {
                try {
                    IConnectionListener listener = (IConnectionListener) listeners[i];
                    if (listener != null) listener.onConnectionFail(ip, port, feConfig, e);
                } catch (Throwable re) {
                    logger.warn("exception received from listener " + listeners[i] + " when dispatching event", re);
                }
            }
        }
    }

    @Override
    public void onConnectionSuccess(String ip, int port, FrontEndConfig feConfig) {
        Object[] listeners = getListeners();

        if (listeners != null) {
            for (int i = 0; i < listeners.length; i++) {
                try {
                    IConnectionListener listener = (IConnectionListener) listeners[i];
                    if (listener != null) listener.onConnectionSuccess(ip, port, feConfig);
                } catch (Throwable re) {
                    logger.warn("exception received from listener " + listeners[i] + " when dispatching event", re);
                }
            }
        }
    }
}
