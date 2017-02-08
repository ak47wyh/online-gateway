package com.iboxpay.settlement.gateway.jd.service.model;

import java.math.BigDecimal;

public class JdQrReqParam extends JdGatewayParam{

	/** 
	 * 商户订单号 每次请求支付必须不相同。
	 */
	private String orderNo ;
	/**
	 * 订单金额 如果确定订单金额,在支付的时候，京东钱包客户端会要求用户自由输入一个金额完成支付 测试商户号，京东风控限额单笔不超过10元
	 */
	private BigDecimal amount ;
	/**
	 * 交易摘要 替换为自己的交易摘要 必须传递
	 */
	private String tradeName ;
	/**
	 * 交易详细描述 替换为自己的交易描述
	 */
	private String tradeDescrible;
	/**
	 *  二维码有效时间 单位为分钟 二维码从生成到扫码支付这个时间段如果超过这个时间，会提示二维码失效
	 */
	private long expire;
	/**
	 * 异步通知回调地址
	 */
	private String notifyUrl;
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
	public long getExpire() {
		return expire;
	}
	public void setExpire(long expire) {
		this.expire = expire;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
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
