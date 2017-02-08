package com.iboxpay.settlement.gateway.alipay.servie.model;

public class WapOrderReqParam extends BaseReqParam{	
	/**
	 * 页面跳转同步通知页面路径
	 */
	private String return_url;
	/**
	 * 卖家支付宝用户号
	 */
	private String seller_id;
	/**
	 * 支付类型
	 */
	private String payment_type;
	/**
	 * 商户网站唯一订单号
	 */
	private String out_trade_no;
	/**
	 * 商品名称
	 */
	private String subject;
	/**
	 * 商品名称
	 */
	private String total_fee;
	/**
	 * 商品展示网址
	 */
	private String show_url;
	/**
	 * 商品描述
	 */
	private String body;
	
	public String getReturn_url() {
		return return_url;
	}
	public void setReturn_url(String return_url) {
		this.return_url = return_url;
	}
	public String getSeller_id() {
		return seller_id;
	}
	public void setSeller_id(String seller_id) {
		this.seller_id = seller_id;
	}
	public String getPayment_type() {
		return payment_type;
	}
	public void setPayment_type(String payment_type) {
		this.payment_type = payment_type;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getTotal_fee() {
		return total_fee;
	}
	public void setTotal_fee(String total_fee) {
		this.total_fee = total_fee;
	}
	public String getShow_url() {
		return show_url;
	}
	public void setShow_url(String show_url) {
		this.show_url = show_url;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	
}

