package com.iboxpay.settlement.gateway.alipay.servie.model;

public class PreCreateRespParam extends BaseRespParam {
	
	private String trade_no;
	
	private String out_trade_no;
	
	private String voucher_type;
	
	private String qr_code;
	
	private String big_pic_url;
	
	private String pic_url;
	
	private String small_pic_url;

	public String getTrade_no() {
		return trade_no;
	}

	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}

	public String getOut_trade_no() {
		return out_trade_no;
	}

	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}

	public String getVoucher_type() {
		return voucher_type;
	}

	public void setVoucher_type(String voucher_type) {
		this.voucher_type = voucher_type;
	}

	public String getQr_code() {
		return qr_code;
	}

	public void setQr_code(String qr_code) {
		this.qr_code = qr_code;
	}

	public String getPic_url() {
		return pic_url;
	}

	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}

	public String getSmall_pic_url() {
		return small_pic_url;
	}

	public void setSmall_pic_url(String small_pic_url) {
		this.small_pic_url = small_pic_url;
	}

	public String getBig_pic_url() {
		return big_pic_url;
	}

	public void setBig_pic_url(String big_pic_url) {
		this.big_pic_url = big_pic_url;
	}
	
}
