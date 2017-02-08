package com.iboxpay.settlement.gateway.jd.service.model;

public class JdQrRespParam extends CommonRespParam{
	/**
	 * 二维码图片原始字符串
	 */
	private String qrcode ;

	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}
	
}
