package com.iboxpay.settlement.gateway.common.inout.callback;

import java.util.Map;

import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;

public class CallbackPaymentRequestModel extends CommonRequestModel{

	private static final long serialVersionUID = 1L;
	private String outTradeNo;
	private String resultCode;
	private String errCode;
	private String errCodeDes;
	private Map<String,Object> resultMap;

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getErrCodeDes() {
		return errCodeDes;
	}

	public void setErrCodeDes(String errCodeDes) {
		this.errCodeDes = errCodeDes;
	}

	public Map<String, Object> getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map<String, Object> resultMap) {
		this.resultMap = resultMap;
	}


	
	
	
	
	
}
