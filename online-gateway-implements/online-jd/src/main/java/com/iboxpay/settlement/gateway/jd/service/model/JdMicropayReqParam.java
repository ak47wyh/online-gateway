package com.iboxpay.settlement.gateway.jd.service.model;

import java.math.BigDecimal;

public class JdMicropayReqParam extends JdGatewayParam {

	/**
	 * 订单号
	 */
	private String orderNo;
	/**
	 * 用户付款码二维码内容
	 */
	private String seed;
	/**
	 * 订单金额
	 */
	private BigDecimal amount;

	/**
	 * 交易名称
	 */
	private String tradeName ;
	/**
	 * 交易描述
	 */
	private String tradeDescrible ;

	/**
	 * 门店号
	 */
	private String subMer ;
	/**
	 * 机具号
	 */
	private String termNo ;

	
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getSeed() {
		return seed;
	}
	public void setSeed(String seed) {
		this.seed = seed;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getTradeName() {
		return tradeName;
	}
	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}
	public String getTradeDescrible() {
		return tradeDescrible;
	}
	public void setTradeDescrible(String tradeDescrible) {
		this.tradeDescrible = tradeDescrible;
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
	
	
	
}
