package com.iboxpay.settlement.gateway.alipay.servie.api;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.beanutils.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.common.json.MapperUtils;
import com.iboxpay.settlement.gateway.alipay.servie.model.AlipayGatewayParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.BaseReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.CancelTradeReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.CancelTradeRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.NotifyVerifyReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.PreCreateReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.PreCreateRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.QueryStatusReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.QueryStatusRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.RefundTradeReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.RefundTradeRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.utils.AlipayPayUtils;
import com.iboxpay.settlement.gateway.alipay.servie.utils.AlipayXmlUtils;
import com.iboxpay.settlement.gateway.alipay.servie.utils.SignatureUtils;

/**
 * 支付宝收单远程接口
 * 
 * @author liaoxiongjian
 * @date 2016-02-04 15:11
 */
@Service("alipayQrTradeRemoteService")
public class AlipayQrTradeRemoteServiceImpl implements AlipayQrTradeRemoteService {
    private static final Logger logger = LoggerFactory.getLogger(AlipayQrTradeRemoteServiceImpl.class);

    @Override
    public PreCreateRespParam doPreCreate(PreCreateReqParam req,AlipayGatewayParam gatewayParam) {
        PreCreateRespParam resp = null;
        try {
            String respTxt = callRemoteService(req,gatewayParam);

            resp = AlipayXmlUtils.parsePreCreate(respTxt);

            logger.info("resp text for doPreCreate from remote alipay service: {}", MapperUtils.toJson(resp));
        } catch (Exception e) {
            logger.error("exception is ", e);
        }

        return resp;
    }

    @Override
    public QueryStatusRespParam doQueryStatus(QueryStatusReqParam req,AlipayGatewayParam gatewayParam) {
        QueryStatusRespParam resp = null;

        try {
            String respTxt = callRemoteService(req,gatewayParam);
            logger.info("resp text for doPreCreate from remote alipay service: {}", respTxt);
            resp = AlipayXmlUtils.parseQueryStatus(respTxt);

            logger.info("resp text for doQueryStatus from remote alipay service: {}", MapperUtils.toJson(resp));
        } catch (Exception e) {
            logger.error("exception is ", e);
        }

        return resp;
    }

    @Override
    public CancelTradeRespParam doCancelTrade(CancelTradeReqParam req,AlipayGatewayParam gatewayParam) {
        CancelTradeRespParam resp = null;

        try {
            String respTxt = callRemoteService(req,gatewayParam);

            resp = AlipayXmlUtils.parseCancelTrade(respTxt);
        } catch (Exception e) {
            logger.error("exception is ", e);
        }

        return resp;
    }
    
    
    public RefundTradeRespParam doRefundTrade(RefundTradeReqParam req,AlipayGatewayParam gatewayParam){
    	
    	RefundTradeRespParam resp = null;
    	 try {
             String respTxt = callRemoteService(req,gatewayParam);
             logger.info("resp text for doRefundTrade from remote alipay service: {}", respTxt);
             resp = AlipayXmlUtils.parseRefundTrade(respTxt);
         } catch (Exception e) {
             logger.error("exception is ", e);
         }

    	 return resp;
    }

    @SuppressWarnings("unchecked")
    private String callRemoteService(BaseReqParam param,AlipayGatewayParam gatewayParam) {
        // 生成签名字符串
        Map<String, Object> beanMap = new BeanMap(param);

        TreeMap<String, Object> treeMap = new TreeMap<String, Object>(beanMap);
        if (treeMap.containsKey("sign")) {
            treeMap.remove("sign");
        }
        if (treeMap.containsKey("sign_type")) {
            treeMap.remove("sign_type");
        }
        if (treeMap.containsKey("class")) {
            treeMap.remove("class");
        }
        StringBuffer sb = new StringBuffer();
        String key = treeMap.firstKey();
        sb.append(key).append("=").append(treeMap.get(key));
        while ((key = treeMap.higherKey(key)) != null) {
            if (treeMap.get(key) != null) {
                sb.append("&").append(key).append("=").append(treeMap.get(key));
            }
        }

        String sign = SignatureUtils.sign(sb.toString(), SignatureUtils.SIGNATURE_MD5, gatewayParam.getSignMd5Key());
        treeMap.put("sign", sign.toLowerCase());
        treeMap.put("sign_type", gatewayParam.getSignType());
        logger.info("before call remote alipay service: {}", MapperUtils.toJson(treeMap));
        try {
            String respTxt =AlipayPayUtils.newHttpClientResponseCharset(gatewayParam.getGatewayUrl(), treeMap,Integer.parseInt(gatewayParam.getRequestTimeout()));
            return respTxt;
        } catch (Exception e) {
            logger.error("exception is ", e);
        }
        return null;
    }

    @Override
    public boolean doNotifyVerify(NotifyVerifyReqParam req,AlipayGatewayParam gatewayParam) {

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("service", req.getService());
        paramMap.put("partner", req.getPartner());
        paramMap.put("notify_id", req.getNotify_id());

        try {
            String respTxt =AlipayPayUtils.newHttpClientResponseCharset(gatewayParam.getGatewayUrl(), paramMap,Integer.parseInt(gatewayParam.getRequestTimeout()));
            logger.info("resp text for doNotifyVerify from remote alipay service: {}", respTxt);

            return Boolean.parseBoolean(respTxt.trim().toLowerCase());
        } catch (Exception e) {
            logger.error("exception is ", e);
        }
        return false;
    }
}
