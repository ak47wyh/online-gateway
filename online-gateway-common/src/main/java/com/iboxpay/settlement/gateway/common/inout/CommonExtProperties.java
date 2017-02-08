package com.iboxpay.settlement.gateway.common.inout;

import java.io.Serializable;

/**
 * 扩展属性公共类
 * @author liaoxiongjian
 * @date 2016-1-29
 */
public class CommonExtProperties implements Serializable {

	private static final long serialVersionUID = 1L;
    /**
     * 证件类型
     */
    private String certType;
    /**
     * 证件号码
     */
    private String certNo;
    /**
     * 手机号码
     */
    private String mobileNo;
	/**
	 * 二维码图片
	 */
	private String codeImgUrl;
	/**
	 * 微信二维码
	 */
	private String codeUrl;
	/**
	 * 授权码
	 */
	private String authCode;
	/**
	 * 产品信息
	 */
	private String productInfo;
	
	
	
	public String getCertType() {
		return certType;
	}

	public void setCertType(String certType) {
		this.certType = certType;
	}

	public String getCertNo() {
		return certNo;
	}

	public void setCertNo(String certNo) {
		this.certNo = certNo;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getCodeImgUrl() {
		return codeImgUrl;
	}

	public void setCodeImgUrl(String codeImgUrl) {
		this.codeImgUrl = codeImgUrl;
	}

	public String getCodeUrl() {
		return codeUrl;
	}

	public void setCodeUrl(String codeUrl) {
		this.codeUrl = codeUrl;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getProductInfo() {
		return productInfo;
	}

	public void setProductInfo(String productInfo) {
		this.productInfo = productInfo;
	}
}
