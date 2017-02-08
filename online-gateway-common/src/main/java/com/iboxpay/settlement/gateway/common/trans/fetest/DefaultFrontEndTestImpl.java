package com.iboxpay.settlement.gateway.common.trans.fetest;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.util.IOUtils;

public class DefaultFrontEndTestImpl implements IFrontEndTest {

    @Override
    public boolean testConnection(FrontEndConfig feConfig) {
        String ip = feConfig.getIp().getVal();
        int port = feConfig.getPort().getIntVal();
        return IOUtils.testConnection(ip, port);
    }
}
