package com.iboxpay.settlement.gateway.jd.service.api.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.jd.service.api.JdQrTradeRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdNotifyVerifyReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdNotifyVerifyRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQrReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQrRespParam;
import com.iboxpay.settlement.gateway.jd.service.utils.HttpUtils;
import com.iboxpay.settlement.gateway.jd.service.utils.JdPayJsonUtils;
import com.iboxpay.settlement.gateway.jd.service.utils.JdPayUtils;
import com.iboxpay.settlement.gateway.jd.service.utils.MD5Util;
import com.iboxpay.settlement.gateway.jd.service.utils.SignUtils;

@Service
public class JdQrTradeRemoteServiceImpl implements JdQrTradeRemoteService {

    private static Logger logger = LoggerFactory.getLogger(JdQrTradeRemoteServiceImpl.class);

    @Override
    public JdQrRespParam doPreCreate(JdQrReqParam reqParam) {
        String gatewayUrl = reqParam.getGatewayUrl();
        String notifyUrl = reqParam.getNotifyUrl();
        try {
            notifyUrl = URLEncoder.encode(notifyUrl, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            logger.error("url编码异常:" + e1.toString(), e1);
        }
        /*
         * 生成签名
         */
        Map<String, String> map = new TreeMap<String, String>();
        map.put("order_no", reqParam.getOrderNo());
        map.put("merchant_no", reqParam.getMerchantNo());
        map.put("amount", reqParam.getAmount() + "");
        map.put("trade_name", reqParam.getTradeName());
        map.put("trade_describle", reqParam.getTradeDescrible());
        map.put("expire", reqParam.getExpire() + "");
        map.put("notify_url", notifyUrl);
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
            logger.error("获取二维码HttpPost编码错误:" + e.toString(), e);
            return null;
        }
        HttpPost httpPost = new HttpPost(gatewayUrl);
        httpPost.setEntity(reqEntity);
        logger.info("获取二维码请求:" + reqEntity.toString());
        String result = HttpUtils.exctueRequest(httpPost);
        logger.info("获取二维码请求返回:" + result);
        if (result == null) {
            return null;
        }

        // 解析扫一扫报文反馈
        JdQrRespParam respParam = JdPayJsonUtils.parsePreCreateResponse(result);
        return respParam;
    }

    @Override
    public JdNotifyVerifyRespParam doNotifyVerify(JdNotifyVerifyReqParam reqParam) {
        String key = reqParam.getMd5Key();
        JdNotifyVerifyRespParam respParam = null;
        try {
            // 验证签名
            String generateSign = MD5Util.md5LowerCase(reqParam.getData(), key);
            if (!generateSign.equalsIgnoreCase(reqParam.getSign())) {
                logger.error("签名验证错误!");
            }
            byte[] decryptBASE64Arr = Base64.decodeBase64(reqParam.getData());
            String result = new String(decryptBASE64Arr);
            logger.info("base64 解码数据:" + result);

            respParam = JdPayJsonUtils.parseNotifyVerifyResponse(result);
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return respParam;
    }

}
