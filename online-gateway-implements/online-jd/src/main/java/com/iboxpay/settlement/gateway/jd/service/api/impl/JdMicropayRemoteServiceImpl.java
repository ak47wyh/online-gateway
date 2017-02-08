package com.iboxpay.settlement.gateway.jd.service.api.impl;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.jd.service.api.JdMicropayRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdMicropayReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdMicropayRespParam;
import com.iboxpay.settlement.gateway.jd.service.utils.HttpUtils;
import com.iboxpay.settlement.gateway.jd.service.utils.JdPayJsonUtils;
import com.iboxpay.settlement.gateway.jd.service.utils.JdPayUtils;
import com.iboxpay.settlement.gateway.jd.service.utils.MD5Util;
import com.iboxpay.settlement.gateway.jd.service.utils.SignUtils;

/**
 * 京东钱包-付款码API
 * @author liaoxiongjian
 * @date 2016-03-23 17:20
 */
@Service
public class JdMicropayRemoteServiceImpl implements JdMicropayRemoteService {

    private static Logger logger = LoggerFactory.getLogger(JdMicropayRemoteServiceImpl.class);

    @Override
    public JdMicropayRespParam doMicroPay(JdMicropayReqParam reqParam) {
        /**
         * 生成签名
         */
        Map<String, String> map = new TreeMap<String, String>();
        map.put("order_no", reqParam.getOrderNo());
        map.put("merchant_no", reqParam.getMerchantNo());
        map.put("amount", reqParam.getAmount() + "");
        map.put("seed", reqParam.getSeed());
        map.put("notify_url", reqParam.getNotifyUrl());
        map.put("trade_name", reqParam.getTradeName());
        map.put("trade_describle", reqParam.getTradeDescrible());
        map.put("sub_mer", reqParam.getSubMer());
        map.put("term_no", reqParam.getTermNo());
        Map<String, String> param = SignUtils.paraFilter(map);//空的参数不参与签名
        String source_ = SignUtils.paraToString(param);
        String sign1_ = null;
        try {
            logger.info("签名原始串:" + source_);
            sign1_ = MD5Util.md5LowerCase(source_, reqParam.getSignMd5Key());
            logger.info("签名值:" + sign1_);
        } catch (Exception e1) {
            logger.error("签名异常:" + e1.toString(), e1);
        }
        param.put("sign", sign1_);
        HttpEntity reqEntity = null;
        try {
            reqEntity = JdPayUtils.buildMapParam(param);
        } catch (UnsupportedEncodingException e) {
            logger.error("封装请求参数异常:" + e.toString(), e);
        }

        HttpPost httpPost = new HttpPost(reqParam.getGatewayUrl());
        httpPost.setEntity(reqEntity);
        logger.info("请求扣用户的钱");
        String result = HttpUtils.exctueRequest(httpPost);
        logger.info("请求扣用户的钱返回:" + result);
        if (result == null) {
            return null;
        }

        // 解析反馈
        JdMicropayRespParam resp = JdPayJsonUtils.parseMicropayResponse(result);
        return resp;
    }
}
