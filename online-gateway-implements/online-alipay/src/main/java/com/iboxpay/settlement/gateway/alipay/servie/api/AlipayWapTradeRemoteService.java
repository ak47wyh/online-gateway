package com.iboxpay.settlement.gateway.alipay.servie.api;

import com.iboxpay.settlement.gateway.alipay.servie.model.AlipayGatewayParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.WapOrderReqParam;

public interface AlipayWapTradeRemoteService {
	public String doWapOrder(WapOrderReqParam param,AlipayGatewayParam gatewayParam);
}
