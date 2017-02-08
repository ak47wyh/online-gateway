package com.iboxpay.settlement.gateway.alipay.servie.model;

/**
 * 基本请求参数
 * @author Jim Yang (oraclebone@gmail.com)
 *
 */
public class BaseReqParam {

	/**
	 * 接口名称
	 */
	private String service;
	
	/**
	 * 合作者身份ID
	 */
	private String partner;
	
	/**
	 * 参数编码字符集
	 */
	private String _input_charset;
	
	/**
	 * 签名方式
	 */
	private String sign_type;
	
	/**
	 * 签名
	 */
	private String sign;
	
	/**
	 * 服务器异步通知页面路径
	 */
	private String notify_url;
	
	/**
	 * 签名类型
	 */
	private String alipay_ca_request;

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
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

	public String getNotify_url() {
		return notify_url;
	}

	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}

	public String getAlipay_ca_request() {
		return alipay_ca_request;
	}

	public void setAlipay_ca_request(String alipay_ca_request) {
		this.alipay_ca_request = alipay_ca_request;
	}

	public String get_input_charset() {
		return _input_charset;
	}

	public void set_input_charset(String _input_charset) {
		this._input_charset = _input_charset;
	}
	
	
}
