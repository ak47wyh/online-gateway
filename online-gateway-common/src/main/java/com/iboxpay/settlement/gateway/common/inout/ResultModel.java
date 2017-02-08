package com.iboxpay.settlement.gateway.common.inout;

public class ResultModel {

    public final static String STATUS_SUCCESS = "1";
    public final static String STATUS_FAIL = "2";
    public final static String STATUS_UNKNOW = "3";

    private String status;//状态1:成功 2:失败 3:未确定
    private String rspcod;//返回码
    private String rspmsg;//返回信息
    private String iboxpayErrorCode;//盒子返回码
    private String accntSettleId;//传递id

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRspcod() {
        return rspcod;
    }

    public void setRspcod(String rspcod) {
        this.rspcod = rspcod;
    }

    public String getRspmsg() {
        return rspmsg;
    }

    public void setRspmsg(String rspmsg) {
        this.rspmsg = rspmsg;
    }

    public String getIboxpayErrorCode() {
        return iboxpayErrorCode;
    }

    public void setIboxpayErrorCode(String iboxpayErrorCode) {
        this.iboxpayErrorCode = iboxpayErrorCode;
    }

    public String getAccntSettleId() {
        return accntSettleId;
    }

    public void setAccntSettleId(String accntSettleId) {
        this.accntSettleId = accntSettleId;
    }

    public static ResultModel fail(String errorMsg) {
        ResultModel result = new ResultModel();
        result.rspcod = STATUS_FAIL;
        result.rspmsg = errorMsg;
        return result;
    }
}
