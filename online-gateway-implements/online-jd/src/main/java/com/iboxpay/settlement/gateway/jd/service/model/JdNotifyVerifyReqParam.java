package com.iboxpay.settlement.gateway.jd.service.model;

public class JdNotifyVerifyReqParam extends JdGatewayParam {

    private String sign;
    private String data;
    private String md5Key;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMd5Key() {
        return md5Key;
    }

    public void setMd5Key(String md5Key) {
        this.md5Key = md5Key;
    }

}
