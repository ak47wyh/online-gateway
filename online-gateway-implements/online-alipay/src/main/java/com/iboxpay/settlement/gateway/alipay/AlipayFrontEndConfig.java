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
package com.iboxpay.settlement.gateway.alipay;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;
/**
 * 矩阵渠道前置机配置
 * @author liaoxiongjian
 * @date 2016年1月23日 下午2:00:23
 * @Version 1.0
 */
public class AlipayFrontEndConfig extends FrontEndConfig {

    private static final long serialVersionUID = 1L;
    /**服务uri*/
    private Property uri;
    

        
    /**
     * 通知url
     */
    public Property notifyUrl;
    
    /**
     * 接口类型-地址
     */
    public Property nativePrecreateService;
    public Property nativeQueryService;
    public Property nativeCancelService;
    public Property nativeNotifyVerify;
    public Property nativeRefundService;
    public Property wapCreateUserService;
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
     * 签名类型:支付宝请求签名类型 1 证书签名； 2 其他密钥签名，默认为2
     */
    public Property alipayCaRequest;
    
    /**
     * 查询超时时间
     */
    public Property queryOverTime;
    
    /********************************************
     * wap 手机支付配置项
     ********************************************/
    // 支付类型
    public Property paymentType;
    // 服务器异步通知页面路径
    public Property wapNotifyUrl;
    // 页面跳转同步通知页面路径
    public Property wapReturnUrl;
    
    
    public AlipayFrontEndConfig() {
        uri = new Property("uri", "http请求的URI");
        notifyUrl = new Property("notifyUrl", "通知url");
        nativePrecreateService = new Property("nativePrecreateService", "接口类型-预下单");
        nativeQueryService = new Property("nativeQueryService", "接口类型-查询");
        nativeCancelService = new Property("nativeCancelService", "接口类型-取消");
        nativeRefundService = new Property("nativeRefundService", "接口类型-退款");
        nativeNotifyVerify = new Property("nativeNotifyVerify", "异步通知");
        wapCreateUserService = new Property("wapCreateUserService","手机网站支付");
        
        version = new Property("version", "版本号【默认1.0】");
        charset = new Property("charset", "字符集【默认UTF-8】");
        signType = new Property("signType", "签名方式【默认MD5】");
        mchCreateIp = new Property("mchCreateIp", "终端IP");
        alipayCaRequest = new Property("alipayCaRequest", "支付宝请求签名类型 1 证书签名； 2 其他密钥签名，默认为2");
        paymentType = new Property("paymentType", "支付类型。仅支持：1（商品购买）");
        wapNotifyUrl = new Property("wapNotifyUrl", "wap服务器异步通知页面路径");
        wapReturnUrl = new Property("wapReturnUrl", "wap页面跳转同步通知页面路径");
        
        queryOverTime = new Property("queryOverTime", "10", "扫码查询超时时间，默认10分钟");
        
        // 设置默认值
        setDefVal(protocal, "https");
        setDefVal(charset, "UTF-8");
        setDefVal(nativePrecreateService, "alipay.acquire.precreate");
        setDefVal(nativeQueryService, "alipay.acquire.query");
        setDefVal(nativeCancelService, "alipay.acquire.cancel");
        setDefVal(nativeCancelService, "alipay.acquire.refund");
        setDefVal(nativeNotifyVerify, "notify_verify");
        setDefVal(wapCreateUserService, "alipay.wap.create.direct.pay.by.user");
        setDefVal(paymentType, "1");
        setDefVal(alipayCaRequest, "2");
    }

	public Property getUri() {
		return uri;
	}

	public Property getNotifyUrl() {
		return notifyUrl;
	}

	public Property getNativePrecreateService() {
		return nativePrecreateService;
	}

	public Property getNativeQueryService() {
		return nativeQueryService;
	}

	public Property getNativeCancelService() {
		return nativeCancelService;
	}

	
	public Property getNativeRefundService() {
		return nativeRefundService;
	}

	public Property getNativeNotifyVerify() {
		return nativeNotifyVerify;
	}

	public Property getWapCreateUserService() {
		return wapCreateUserService;
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

	public Property getAlipayCaRequest() {
		return alipayCaRequest;
	}

	public Property getPaymentType() {
		return paymentType;
	}

	public Property getWapNotifyUrl() {
		return wapNotifyUrl;
	}

	public Property getWapReturnUrl() {
		return wapReturnUrl;
	}

	public Property getQueryOverTime() {
		return queryOverTime;
	}
	
	
	
}

