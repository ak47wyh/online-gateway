package com.iboxpay.settlement.gateway.common.net;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.exception.FrontEndException;

/**
 * 连接监听器
 * @author jianbo_chen
 */
public interface IConnectionListener {

    void onConnectionFail(String ip, int port, FrontEndConfig feConfig, FrontEndException e);

    void onConnectionSuccess(String ip, int port, FrontEndConfig feConfig);
}
