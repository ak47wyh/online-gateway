package com.iboxpay.settlement.gateway.alipay.servie.model;

public class AlipayGatewayParam {
	
    // 支付网关地址
    private String gatewayUrl;
    // 签名类型
    private String signType;
    // 签名密钥
    private String signMd5Key;
    // 请求超时时间
    private String requestTimeout;
    
	public String getGatewayUrl() {
		return gatewayUrl;
	}
	public void setGatewayUrl(String gatewayUrl) {
		this.gatewayUrl = gatewayUrl;
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
	}
	public String getSignMd5Key() {
		return signMd5Key;
	}
	public void setSignMd5Key(String signMd5Key) {
		this.signMd5Key = signMd5Key;
	}
	public String getRequestTimeout() {
		return requestTimeout;
	}
	public void setRequestTimeout(String requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
}
