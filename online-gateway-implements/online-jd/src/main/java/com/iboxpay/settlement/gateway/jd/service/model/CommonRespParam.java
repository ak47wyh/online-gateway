package com.iboxpay.settlement.gateway.jd.service.model;

public class CommonRespParam {
	// 成功标志
	private String isSuccess;
	// 失败code
	private String errorCode;
	// 失败原因
	private String errorDes;
	
	public String getIsSuccess() {
		return isSuccess;
	}
	public void setIsSuccess(String isSuccess) {
		this.isSuccess = isSuccess;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorDes() {
		return errorDes;
	}
	public void setErrorDes(String errorDes) {
		this.errorDes = errorDes;
	}
}
