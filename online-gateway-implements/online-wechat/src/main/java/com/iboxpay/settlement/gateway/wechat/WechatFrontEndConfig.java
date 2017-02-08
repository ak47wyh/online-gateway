/*
 * Copyright (C) 2011-2015 ShenZhen iBOXPAY Information Technology Co.,Ltd.
 * 
 * All right reserved.
 * 
 * This software is the confidential and proprietary
 * information of iBoxPay Company of China. 
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement 
 * you entered into with iBoxpay inc.
 *
 */
package com.iboxpay.settlement.gateway.wechat;

import java.io.File;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.net.HttpsContext;
/**
 * 微信支付前置机配置-威富通
 * @author liaoxiongjian
 * @date 2016年1月23日 下午2:00:23
 * @Version 1.0
 */
public class WechatFrontEndConfig extends FrontEndConfig implements HttpsContext {

    private static final long serialVersionUID = 1L;
    /**服务uri*/
    private Property uri;
                
    /**
     * 通知url
     */
    public Property notifyUrl;
    
    /**
     * 接口类型-
     */
    public Property nativePayUrl;
    public Property nativeQueryUrl;
    public Property nativeRefundUrl;
    public Property nativeRefundQueryUrl;
    public Property micropayPayUrl;
    public Property micropayQueryUrl;
    public Property micropayReverseUrl;
    public Property closeUrl;
    
    /**
     * 版本号
     */
    public Property version;
    /**
     * 签名方式
     */
    public Property signType;
    /**
     * 终端IP
     */
    public Property mchCreateIp;
    
    /**
     * 查询次数限制
     */
    public Property queryTransCount;
    
    /**
     * 查询时间间隔
     */
    public Property queryInterval;
    /**
     * 查询超时时间
     */
    public Property queryOverTime;
    
    
    /**私匙证书格式 */
    private Property keyStoreType;
    /**私钥保存文件*/
    private Property keyStoreFile;
    /**私匙保存库密码*/
    private Property keyStorePassword;
    /**私匙密码*/
    private Property keyPassword;
    /**是否信任公钥证书 */
    private Property trustCertification;
    
    public WechatFrontEndConfig() {
        uri = new Property("uri", "http请求的URI");
        notifyUrl = new Property("notifyUrl", "通知url");
        nativePayUrl = new Property("nativePayUrl", "扫码支付地址");
        nativeQueryUrl = new Property("nativeQueryUrl", "扫码查询地址");
        nativeRefundUrl = new Property("nativeRefundUrl", "扫码退款地址");
        nativeRefundQueryUrl =new Property("nativeRefundQueryUrl","查询退款状态地址");
        micropayPayUrl = new Property("micropayPayUrl", "刷卡支付地址");
        micropayQueryUrl = new Property("micropayQueryUrl", "刷卡查询地址");
        micropayReverseUrl = new Property("micropayReverseUrl", "刷卡冲正地址");
        closeUrl = new Property("closeUrl","关闭订单地址");
        
        version = new Property("version", "版本号【默认1.0】");
        charset = new Property("charset", "字符集【默认UTF-8】");
        signType = new Property("signType", "签名方式【默认MD5】");
        mchCreateIp = new Property("mchCreateIp", "终端IP");
        queryTransCount = new Property("queryTransCount", "3", "查询微信次数，默认3次");
        queryInterval = new Property("queryInterval", "10", "发起交易后，多长时间（秒）可以查询交易状态，默认10秒");
        queryOverTime = new Property("queryOverTime", "10", "扫码查询超时时间，默认10分钟");
        
        keyStoreType = new Property("keyStoreType", "私匙证书格式(如“PKCS12”)");
        keyStoreFile = new Property("keyStoreFile", Property.Type.file, "私钥保存文件");
        keyStorePassword = new Property("keyStorePassword", "私匙保存库密码");
        keyPassword = new Property("keyPassword", "私匙密码");
        trustCertification = new Property("trustCertification", "是否信任公钥证书（包括域名校验. “true” 或  “false”）");
        
        
		setDefVal(protocal, "https");
		setDefVal(charset, "UTF-8");
		setDefVal(version, "1.0");
		setDefVal(signType, "MD5");
		setDefVal(nativePayUrl, "/pay/unifiedorder");
		setDefVal(nativeQueryUrl, "/pay/orderquery");
		setDefVal(nativeRefundUrl, "/secapi/pay/refund");
		setDefVal(nativeRefundQueryUrl, "/pay/refundquery");
		setDefVal(micropayPayUrl, "/pay/micropay");
		setDefVal(micropayQueryUrl, "/pay/orderquery");
		setDefVal(micropayReverseUrl, "/secapi/pay/reverse");
		setDefVal(closeUrl, "/pay/closeorder");
    }

	public Property getUri() {
		return uri;
	}


	public Property getNotifyUrl() {
		return notifyUrl;
	}

	public Property getNativePayUrl() {
		return nativePayUrl;
	}

	public Property getNativeQueryUrl() {
		return nativeQueryUrl;
	}

	public Property getNativeRefundUrl() {
		return nativeRefundUrl;
	}

	public Property getNativeRefundQueryUrl() {
		return nativeRefundQueryUrl;
	}

	public Property getMicropayPayUrl() {
		return micropayPayUrl;
	}

	public Property getMicropayQueryUrl() {
		return micropayQueryUrl;
	}

	public Property getMicropayReverseUrl() {
		return micropayReverseUrl;
	}

	public Property getCloseUrl() {
		return closeUrl;
	}

	public Property getVersion() {
		return version;
	}

	public Property getCharset() {
		return charset;
	}

	public Property getSignType() {
		return signType;
	}

	public Property getMchCreateIp() {
		return mchCreateIp;
	}

	public Property getQueryTransCount() {
		return queryTransCount;
	}

	public Property getQueryInterval() {
		return queryInterval;
	}

	public Property getQueryOverTime() {
		return queryOverTime;
	}

	public Property getKeyStoreType() {
		return keyStoreType;
	}

	public Property getKeyStoreFile() {
		return keyStoreFile;
	}

	public Property getKeyStorePassword() {
		return keyStorePassword;
	}

	public Property getKeyPassword() {
		return keyPassword;
	}

	public Property getTrustCertification() {
		return trustCertification;
	}

	@Override
	public String keyStoreType() {
		 return keyStoreType.getVal();
	}

	@Override
	public File keyStoreFile() {
		return keyStoreFile.getFileVal();
	}

	@Override
	public String keyStorePassword() {
		return keyStorePassword.getVal();
	}

	@Override
	public String keyPassword() {
		return keyPassword.getVal();
	}

	@Override
	public boolean trustCertification() {
		 return Boolean.valueOf(trustCertification.getVal());
	}


	
	

}

