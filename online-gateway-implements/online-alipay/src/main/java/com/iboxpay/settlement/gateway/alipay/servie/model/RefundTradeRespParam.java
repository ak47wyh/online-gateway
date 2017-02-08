package com.iboxpay.settlement.gateway.alipay.servie.model;

public class RefundTradeRespParam extends BaseRespParam{
	
	private String out_trade_no;
	private String trade_no;
	private String buyer_user_id;
	private String buyer_logon_id;
	private String fund_change;
	
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getTrade_no() {
		return trade_no;
	}
	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}
	public String getBuyer_user_id() {
		return buyer_user_id;
	}
	public void setBuyer_user_id(String buyer_user_id) {
		this.buyer_user_id = buyer_user_id;
	}
	public String getBuyer_logon_id() {
		return buyer_logon_id;
	}
	public void setBuyer_logon_id(String buyer_logon_id) {
		this.buyer_logon_id = buyer_logon_id;
	}
	public String getFund_change() {
		return fund_change;
	}
	public void setFund_change(String fund_change) {
		this.fund_change = fund_change;
	}
	
	
	
}
