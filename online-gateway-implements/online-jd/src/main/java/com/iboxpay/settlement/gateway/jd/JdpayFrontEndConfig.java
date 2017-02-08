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
package com.iboxpay.settlement.gateway.jd;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;

/**
 * 京东钱包前置机配置
 * @author liaoxiongjian
 * @date 2016年3月23日 下午2:00:23
 * @Version 1.0
 */
public class JdpayFrontEndConfig extends FrontEndConfig {

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
    public Property nativePrecreateService;
    public Property nativeQueryService;
    public Property nativeCancelService;
    public Property nativeNotifyVerify;
    public Property nativeRefundService;
    //扫码
    public Property nativePayUrl;
    //刷卡
    public Property micropayPayUrl;
    public Property queryUrl;
    public Property refundUrl;
    public Property refundQueryUrl;
    public Property cancleUrl;
    public Property reverseUrl;
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
     * 签名类型:支付宝请求签名类型 1 证书签名； 2 其他密钥签名，默认为2
     */
    public Property alipayCaRequest;

    public JdpayFrontEndConfig() {
        uri = new Property("uri", "http请求的URI");
        notifyUrl = new Property("notifyUrl", "通知url");

        nativePrecreateService = new Property("nativePrecreateService", "接口类型-预下单");
        nativeQueryService = new Property("nativeQueryService", "接口类型-查询");
        nativeCancelService = new Property("nativeCancelService", "接口类型-取消");
        nativeRefundService = new Property("nativeRefundService", "接口类型-退款");
        nativeNotifyVerify = new Property("nativeNotifyVerify", "异步通知");

        nativePayUrl = new Property("nativePayUrl", "扫码支付地址");
        micropayPayUrl = new Property("micropayPayUrl", "刷卡支付地址");
        queryUrl = new Property("queryUrl", "查询地址");
        refundUrl = new Property("refundUrl", "退款地址");
        refundQueryUrl = new Property("refundQueryUrl", "退款查询地址");
        cancleUrl = new Property("cancleUrl", "撤消地址");
        reverseUrl = new Property("reverseUrl", "冲正地址");
        closeUrl = new Property("closeUrl", "关闭订单地址");

        version = new Property("version", "版本号【默认1.0】");
        charset = new Property("charset", "字符集【默认UTF-8】");
        signType = new Property("signType", "签名方式【默认MD5】");
        mchCreateIp = new Property("mchCreateIp", "终端IP");
        alipayCaRequest = new Property("alipayCaRequest", "支付宝请求签名类型 1 证书签名； 2 其他密钥签名，默认为2");

        // 设置默认值
        setDefVal(protocal, "https");
        setDefVal(charset, "UTF-8");
        setDefVal(nativePrecreateService, "alipay.acquire.precreate");
        setDefVal(nativeQueryService, "alipay.acquire.query");
        setDefVal(nativeCancelService, "alipay.acquire.cancel");
        setDefVal(nativeCancelService, "alipay.acquire.refund");
        setDefVal(nativeNotifyVerify, "notify_verify");
        setDefVal(alipayCaRequest, "2");

        setDefVal(notifyUrl, "http://localhost:18080/online-gateway/jd/notify.htm");
        //业务接口地址
        setDefVal(nativePayUrl, "/pay/unifiedorder");
        setDefVal(micropayPayUrl, "https://pcplatform.jdpay.com/api/pay");
        setDefVal(queryUrl, "https://payscc.jdpay.com/order/query");
        setDefVal(refundUrl, "https://payscc.jdpay.com/order/refund");
        setDefVal(refundQueryUrl, "https://payscc.jdpay.com/order/query/refund");
        setDefVal(cancleUrl, "https://payscc.jdpay.com/order/cancel");
        setDefVal(reverseUrl, "https://payscc.jdpay.com/order/cancel");
        setDefVal(closeUrl, "/pay/closeorder");
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

    public Property getMicropayPayUrl() {
        return micropayPayUrl;
    }

    public Property getQueryUrl() {
        return queryUrl;
    }

    public void setQueryUrl(Property queryUrl) {
        this.queryUrl = queryUrl;
    }

    public Property getRefundUrl() {
        return refundUrl;
    }

    public void setRefundUrl(Property refundUrl) {
        this.refundUrl = refundUrl;
    }

    public Property getRefundQueryUrl() {
        return refundQueryUrl;
    }

    public void setRefundQueryUrl(Property refundQueryUrl) {
        this.refundQueryUrl = refundQueryUrl;
    }

    public Property getReverseUrl() {
        return reverseUrl;
    }

    public void setReverseUrl(Property reverseUrl) {
        this.reverseUrl = reverseUrl;
    }

    public Property getNativePayUrl() {
        return nativePayUrl;
    }

}
