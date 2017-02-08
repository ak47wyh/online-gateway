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
package com.iboxpay.settlement.gateway.wft;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;
/**
 * 微信支付前置机配置-威富通
 * @author liaoxiongjian
 * @date 2016年1月23日 下午2:00:23
 * @Version 1.0
 */
public class WftFrontEndConfig extends FrontEndConfig {

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
    public Property nativePayService;
    public Property nativeQueryService;
    public Property nativieRefundService;
    public Property refundQueryService;
    public Property micropayPayService;
    public Property micropayQueryService;
    public Property micropayReverseService;
    public Property closeService;
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
    
    
    
    public WftFrontEndConfig() {
        uri = new Property("uri", "http请求的URI");
        notifyUrl = new Property("notifyUrl", "通知url");
        nativePayService = new Property("nativePayService", "扫码支付地址");
        nativeQueryService = new Property("nativeQueryService", "扫码查询地址");
        nativieRefundService = new Property("nativieRefundService", "扫码退款地址");
        refundQueryService = new Property("refundQueryService","扫码退款查询地址");
        micropayPayService = new Property("micropayPayService", "刷卡支付地址");
        micropayQueryService = new Property("micropayQueryService", "刷卡查询地址");
        micropayReverseService = new Property("micropayReverseService", "刷卡冲正地址");
        closeService = new Property("closeService","关闭订单地址");
        
        version = new Property("version", "版本号【默认1.0】");
        charset = new Property("charset", "字符集【默认UTF-8】");
        signType = new Property("signType", "签名方式【默认MD5】");
        mchCreateIp = new Property("mchCreateIp", "终端IP");
        queryTransCount = new Property("queryTransCount", "3", "查询微信次数，默认3次");
        queryInterval = new Property("queryInterval", "10", "发起交易后，多长时间（秒）可以查询交易状态，默认10秒");
        queryOverTime = new Property("queryOverTime", "10", "扫码查询超时时间，默认10分钟");
        
		setDefVal(protocal, "https");
		setDefVal(charset, "UTF-8");
		setDefVal(version, "1.0");
		setDefVal(signType, "MD5");
		setDefVal(nativePayService, "pay.weixin.native");
		setDefVal(nativieRefundService, "trade.single.refund");
		setDefVal(refundQueryService,"trade.refund.query");
		setDefVal(micropayPayService, "pay.weixin.micropay");
		setDefVal(micropayQueryService, "pay.weixin.micropay");
		setDefVal(micropayReverseService, "pay.weixin.micropay.reverse");
		setDefVal(closeService, "pay.weixin.micropay.close");
    }

	public Property getUri() {
		return uri;
	}

	public Property getNotifyUrl() {
		return notifyUrl;
	}

	public Property getNativePayService() {
		return nativePayService;
	}

	public Property getNativeQueryService() {
		return nativeQueryService;
	}

	public Property getNativieRefundService() {
		return nativieRefundService;
	}

	public Property getRefundQueryService() {
		return refundQueryService;
	}

	public Property getMicropayPayService() {
		return micropayPayService;
	}

	public Property getMicropayQueryService() {
		return micropayQueryService;
	}

	public Property getMicropayReverseService() {
		return micropayReverseService;
	}

	public Property getCloseService() {
		return closeService;
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

	
    
	
}

