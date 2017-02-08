package com.iboxpay.settlement.gateway.common.trans;

public enum TransCode {
    //业务可以扩展
    BALANCE("balance", "余额查询"), 
    PAY("pay", "支付"), 
    QUERY("query", "同步支付结果"), 
    DETAIL("detail", "交易明细查询"), 
    CHECK("check", "对账"),
    CALLBACK("callback","异步回调"),
    REFUND("refund","退款"),
    REFUNDQUERY("refundquery","退款查询"),
    REVERSE("reverse","冲正"),
    VERIFY("verify","账号验证"),
    CLOSE("close","订单关闭");

    private String code;
    private String desc;

    private TransCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String toString() {
        return ":::" + code + ":::(" + desc + ")";
    }
}
