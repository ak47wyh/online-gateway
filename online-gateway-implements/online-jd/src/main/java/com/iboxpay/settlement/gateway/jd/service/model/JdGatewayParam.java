package com.iboxpay.settlement.gateway.jd.service.model;

public class JdGatewayParam {

    // 支付网关地址
    private String gatewayUrl;
    // 签名类型
    private String merchantNo;
    // 签名密钥
    private String signMd5Key;

    //回调地址
    private String notifyUrl;

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getSignMd5Key() {
        return signMd5Key;
    }

    public void setSignMd5Key(String signMd5Key) {
        this.signMd5Key = signMd5Key;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

}
