package com.iboxpay.settlement.gateway.jd.service.api.impl;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.jd.service.api.JdTradeRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdCancleReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdCancleRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdGatewayParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQueryStatusReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQueryStatusRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdRefundReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdRefundRespParam;
import com.iboxpay.settlement.gateway.jd.service.utils.HttpUtils;
import com.iboxpay.settlement.gateway.jd.service.utils.JdPayJsonUtils;
import com.iboxpay.settlement.gateway.jd.service.utils.JdPayUtils;
import com.iboxpay.settlement.gateway.jd.service.utils.MD5Util;
import com.iboxpay.settlement.gateway.jd.service.utils.SignUtils;

@Service
public class JdTradeRemoteServiceImpl implements JdTradeRemoteService {

    private static Logger logger = LoggerFactory.getLogger(JdTradeRemoteServiceImpl.class);

    /**
     * 查询交易状态
     */
    @Override
    public JdQueryStatusRespParam doQueryStatus(JdQueryStatusReqParam reqParam, JdGatewayParam gatewayParam) {
        JdQueryStatusRespParam respParam = null;
        String merchantNo = gatewayParam.getMerchantNo();
        String md5Key = gatewayParam.getSignMd5Key();
        String gatewayUrl = gatewayParam.getGatewayUrl();
        String orderNo = reqParam.getOrderNo();
        /**
         * 生成签名
         */
        Map<String, String> map = new TreeMap<String, String>();
        map.put("order_no", orderNo);
        map.put("merchant_no", merchantNo);

        Map<String, String> param = SignUtils.paraFilter(map);//空的参数不参与签名
        String source_ = SignUtils.paraToString(param);
        String sign1_ = null;
        try {
            sign1_ = MD5Util.md5LowerCase(source_, md5Key);
        } catch (Exception e1) {
            logger.error("签名异常:" + e1.toString(), e1);
        }
        param.put("sign", sign1_);
        HttpEntity reqEntity = null;
        try {
            reqEntity = JdPayUtils.buildMapParam(param);
        } catch (UnsupportedEncodingException e) {
            logger.error("参数编码异常:" + e.toString(), e);
        }

        String result = "";
        try {
            HttpPost httpPost = new HttpPost(gatewayUrl);
            httpPost.setEntity(reqEntity);
            logger.info("查询用户订单详情");
            result = HttpUtils.exctueRequest(httpPost);
            logger.info("查询用户订单详情:" + result);
        } catch (Exception e) {
            logger.error("exception is ", e);
        }

        // 解析返回的查询数据
        if (!StringUtils.isEmpty(result)) {
            respParam = JdPayJsonUtils.parseQueryStatusResponse(result);
        }
        return respParam;
    }

    @Override
    public JdCancleRespParam doCancelTrade(JdCancleReqParam reqParam, JdGatewayParam gatewayParam) {
        JdCancleRespParam resp = null;
        /*
         * 生成签名
         */
        Map<String, String> map = new TreeMap<String, String>();
        map.put("order_no", reqParam.getOrderNo());
        map.put("merchant_no", gatewayParam.getMerchantNo());
        map.put("cancel_no", reqParam.getCancleNo());
        map.put("amount", reqParam.getAmount());
        map.put("note", reqParam.getNote());

        Map<String, String> param = SignUtils.paraFilter(map);
        String source_ = SignUtils.paraToString(param);
        String sign1_ = null;
        try {
            logger.info("签名原始串:" + source_);
            sign1_ = MD5Util.md5LowerCase(source_, gatewayParam.getSignMd5Key());
            logger.info("签名值:" + sign1_);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            logger.error("签名异常:" + e1.toString(), e1);
        }
        param.put("sign", sign1_);

        HttpEntity reqEntity = null;
        try {
            reqEntity = JdPayUtils.buildMapParam(param);
        } catch (UnsupportedEncodingException e) {
            logger.error("参数编码异常:" + e.toString(), e);
        }
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(gatewayParam.getGatewayUrl());
            httpPost.setEntity(reqEntity);
            logger.info("请求撤销订单");
            result = HttpUtils.exctueRequest(httpPost);
            logger.info("请求撤销订单返回:" + result);
        } catch (Exception e) {
            logger.error("exception is ", e);
        }

        // 解析返回报文数据
        if (!StringUtils.isEmpty(result)) {
            resp = JdPayJsonUtils.parseCancleTradeResponse(result);
        }
        return resp;
    }

    @Override
    public JdRefundRespParam doRefundTrade(JdRefundReqParam reqParam, JdGatewayParam gatewayParam) {
        JdRefundRespParam respParam = null;
        String amount = reqParam.getRefundAmount();
        String merchantNo = gatewayParam.getMerchantNo();
        String orderNo = reqParam.getOrderNo();
        String refundNo = reqParam.getRefundNo();
        String notifyUrl = gatewayParam.getNotifyUrl();
        /*
         * 生成签名
         */
        Map<String, String> map = new TreeMap<String, String>();
        map.put("order_no", orderNo);
        map.put("merchant_no", merchantNo);
        map.put("refund_no", refundNo);
        map.put("amount", amount);
        map.put("notify_url", notifyUrl);
        Map<String, String> param = SignUtils.paraFilter(map);//空的参数不参与签名
        String source_ = SignUtils.paraToString(param);
        String sign1_ = null;
        try {
            logger.info("签名原始串:" + source_);
            sign1_ = MD5Util.md5LowerCase(source_, gatewayParam.getSignMd5Key());
            logger.info("签名值:" + sign1_);
        } catch (Exception e1) {
            logger.error("签名异常:" + e1.toString(), e1);
        }
        param.put("sign", sign1_);
        HttpEntity reqEntity = null;
        try {
            reqEntity = JdPayUtils.buildMapParam(param);
        } catch (UnsupportedEncodingException e) {
            logger.error("参数编码异常:" + e.toString(), e);
        }

        String result = "";
        try {
            HttpPost httpPost = new HttpPost(gatewayParam.getGatewayUrl());
            httpPost.setEntity(reqEntity);
            logger.info("请求退款");
            result = HttpUtils.exctueRequest(httpPost);
            logger.info("请求退款返回:" + result);
        } catch (Exception e) {
            logger.error("exception is ", e);
        }

        // 解析反馈JSON报文数据
        if (!StringUtils.isEmpty(result)) {
            respParam = JdPayJsonUtils.parseRefundTradeResponse(result);
        }
        return respParam;
    }

    @Override
    public JdRefundRespParam doRefundQueryStatus(JdRefundReqParam reqParam, JdGatewayParam gatewayParam) {
        JdRefundRespParam respParam = null;
        Map<String, String> map = new TreeMap<String, String>();
        map.put("refund_no", reqParam.getRefundNo());
        map.put("merchant_no", gatewayParam.getMerchantNo());
        Map<String, String> param = SignUtils.paraFilter(map);// 空的参数不参与签名
        String source_ = SignUtils.paraToString(param);
        String sign1_ = null;
        try {
            logger.info("签名原始串:" + source_);
            sign1_ = MD5Util.md5LowerCase(source_, gatewayParam.getSignMd5Key());
            logger.info("签名值:" + sign1_);
        } catch (Exception e1) {
            logger.error("签名异常:" + e1.toString(), e1);
        }
        param.put("sign", sign1_);
        HttpEntity reqEntity = null;
        try {
            reqEntity = JdPayUtils.buildMapParam(param);
        } catch (UnsupportedEncodingException e) {
            logger.error("参数编码异常:" + e.toString(), e);
        }
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(gatewayParam.getGatewayUrl());
            httpPost.setEntity(reqEntity);
            logger.info("查询退款");
            result = HttpUtils.exctueRequest(httpPost);
            logger.info("查询退款返回:" + result);
        } catch (Exception e) {
            logger.error("exception is ", e);
        }

        // 解析反馈报文数据
        if (!StringUtils.isEmpty(result)) {
            respParam = JdPayJsonUtils.parseQueryRefundResponse(result);
        }

        return respParam;
    }

    @Override
    public boolean doRefundNotifyVerify() {
        return false;
    }

}
