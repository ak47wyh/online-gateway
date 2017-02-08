package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.iboxpay.settlement.gateway.common.util.ClassUtil;

@Entity
@Table(name = "T_EB_PAYMENT_MERCHANT")
public class PaymentMerchantEntity implements Serializable{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paymentmerchant_generator")
	@SequenceGenerator(name = "paymentmerchant_generator", sequenceName = "PAYMENT_MERCHANT_ID_SEQ", allocationSize = 20, initialValue = 1)
	@Column(name = "ID")
	private Long id;// long

	@Column(name = "APP_CODE")
	private String appCode;// VARCHAR2(32) 交易主账号
	
	@Column(name = "APP_ID")
	private String appId;// VARCHAR2(32) 微信公众号
	
	@Column(name = "APP_SECRET")
	private String appSecret;// VARCHAR2(32) 子微信公众号
	
	@Column(name = "SUB_APP_ID")
	private String subAppId;// VARCHAR2(32) 子微信公众号
	
	@Column(name = "SUB_APP_SECRET")
	private String subAppSecret;// VARCHAR2(32) 子微信公众号
	
	@Column(name = "PAY_MERCHANT_NAME")
	private String payMerchantName;// VARCHAR2(32) 交易商户号
	
	@Column(name = "PAY_MERCHANT_NO")
	private String payMerchantNo;// VARCHAR2(32) 交易商户号

	@Column(name = "PAY_MERCHANT_SUB_NO")
	private String payMerchantSubNo;// VARCHAR2(32) 子商户号/代理商编号

	@Column(name = "PAY_MERCHANT_KEY")
	private String payMerchantKey;// VARCHAR2(64) 秘钥
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAppCode() {
		return appCode;
	}
	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public String getAppSecret() {
		return appSecret;
	}
	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}
	
	public String getSubAppId() {
		return subAppId;
	}
	public void setSubAppId(String subAppId) {
		this.subAppId = subAppId;
	}
	
	public String getSubAppSecret() {
		return subAppSecret;
	}
	public void setSubAppSecret(String subAppSecret) {
		this.subAppSecret = subAppSecret;
	}
	public String getPayMerchantNo() {
		return payMerchantNo;
	}
	public void setPayMerchantNo(String payMerchantNo) {
		this.payMerchantNo = payMerchantNo;
	}
	public String getPayMerchantSubNo() {
		return payMerchantSubNo;
	}
	public void setPayMerchantSubNo(String payMerchantSubNo) {
		this.payMerchantSubNo = payMerchantSubNo;
	}
	public String getPayMerchantKey() {
		return payMerchantKey;
	}
	public void setPayMerchantKey(String payMerchantKey) {
		this.payMerchantKey = payMerchantKey;
	}
	
	public String getPayMerchantName() {
		return payMerchantName;
	}

	public void setPayMerchantName(String payMerchantName) {
		this.payMerchantName = payMerchantName;
	}
	@Override
    public String toString() {
        return ClassUtil.toString(this);
    }
}
