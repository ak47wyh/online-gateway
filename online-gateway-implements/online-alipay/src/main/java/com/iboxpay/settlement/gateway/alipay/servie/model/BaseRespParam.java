package com.iboxpay.settlement.gateway.alipay.servie.model;

/**
 * 基本响应参数
 * @author Jim Yang (oraclebone@gmail.com)
 *
 */
public class BaseRespParam {

	/**
	 * 请求是否成功
	 */
	private String is_success;
	
	/**
	 * 签名方式
	 */
	private String sign_type;
	
	/**
	 * 签名
	 */
	private String sign;
	
	/**
	 * 错误代码
	 */
	private String error;
	
	/**
	 * 响应码
	 */
	private String result_code;
	
	/**
	 * 业务错误码
	 */
	private String detail_error_code;
	
	/**
	 * 业务错误描述
	 */
	private String detail_error_des;

	public String getIs_success() {
		return is_success;
	}

	public void setIs_success(String is_success) {
		this.is_success = is_success;
	}

	public String getSign_type() {
		return sign_type;
	}

	public void setSign_type(String sign_type) {
		this.sign_type = sign_type;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getResult_code() {
		return result_code;
	}

	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}

	public String getDetail_error_code() {
		return detail_error_code;
	}

	public void setDetail_error_code(String detail_error_code) {
		this.detail_error_code = detail_error_code;
	}

	public String getDetail_error_des() {
		return detail_error_des;
	}

	public void setDetail_error_des(String detail_error_des) {
		this.detail_error_des = detail_error_des;
	}
	
	
}
