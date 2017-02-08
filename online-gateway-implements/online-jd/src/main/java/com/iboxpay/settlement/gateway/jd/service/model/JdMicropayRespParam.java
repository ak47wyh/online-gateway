package com.iboxpay.settlement.gateway.jd.service.model;

public class JdMicropayRespParam extends CommonRespParam{	
	// 订单号
	private String orderNo;
	// 交易号
	private String tradeNo;
	// 支付用户
	private String user;
	// 支付金额
	private double amount;
	// 营销金额
	private double promotionAmount;
	// 支付商户号
	private String merchantNo;
	// 支付时间
	private String payTime;
	// 门店号
	private String subMer;
	// 机具号
	private String termNo;
	// 额外信息
	private String extraInfo;
	

	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public double getPromotionAmount() {
		return promotionAmount;
	}
	public void setPromotionAmount(double promotionAmount) {
		this.promotionAmount = promotionAmount;
	}
	public String getmerchantNo() {
		return merchantNo;
	}
	public void setmerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}
	public String getPayTime() {
		return payTime;
	}
	public void setPayTime(String payTime) {
		this.payTime = payTime;
	}
	public String getSubMer() {
		return subMer;
	}
	public void setSubMer(String subMer) {
		this.subMer = subMer;
	}
	public String getTermNo() {
		return termNo;
	}
	public void setTermNo(String termNo) {
		this.termNo = termNo;
	}
	public String getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	
}
