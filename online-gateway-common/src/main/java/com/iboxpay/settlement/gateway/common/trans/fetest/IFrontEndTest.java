package com.iboxpay.settlement.gateway.common.trans.fetest;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;

public interface IFrontEndTest {

    public boolean testConnection(FrontEndConfig feConfig);
}
