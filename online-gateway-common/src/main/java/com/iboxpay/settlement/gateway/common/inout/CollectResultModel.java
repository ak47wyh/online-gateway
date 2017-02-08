package com.iboxpay.settlement.gateway.common.inout;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectResultModel implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(CollectResultModel.class);

    private static final long serialVersionUID = 1L;

    //此次请求成功
    public final static String STATUS_SUCCESS = "success";
    //此次请求失败
    public final static String STATUS_FAIL = "fail";

    private String appCode;//交易主账号	交易主账号
    private String status;//状态
    private String errorCode;//错误码
    private String errorMsg;//	状态信息

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
