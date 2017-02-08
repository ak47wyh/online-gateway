package com.iboxpay.settlement.gateway.alipay.servie.model;

public class RefundTradeReqParam extends BaseReqParam{
	// 商家订单号
	private String out_trade_no;
	// 退款金额
	private String refund_amount;

	public String getOut_trade_no() {
		return out_trade_no;
	}

	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}

	public String getRefund_amount() {
		return refund_amount;
	}

	public void setRefund_amount(String refund_amount) {
		this.refund_amount = refund_amount;
	}
	
	
}
