package com.iboxpay.settlement.gateway.common.inout;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.trans.ErrorCode;

public class CommonResultModel implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(CommonResultModel.class);

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

    public CommonResultModel setAppCode(String appCode) {
        this.appCode = appCode;
        return this;
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

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode.name();
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public CommonResultModel fail(ErrorCode errorCode, String statusMsg) {
        logger.error(statusMsg);
        this.setStatus(STATUS_FAIL);
        this.setErrorCode(errorCode);
        this.setErrorMsg(statusMsg);
        return this;
    }
}
