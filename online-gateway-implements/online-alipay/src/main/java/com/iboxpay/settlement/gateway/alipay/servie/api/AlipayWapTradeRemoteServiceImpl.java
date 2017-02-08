package com.iboxpay.settlement.gateway.alipay.servie.api;

import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.beanutils.BeanMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.common.json.MapperUtils;
import com.iboxpay.settlement.gateway.alipay.servie.model.AlipayGatewayParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.BaseReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.WapOrderReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.utils.AlipayPayUtils;
import com.iboxpay.settlement.gateway.alipay.servie.utils.SignatureUtils;

@Service
public class AlipayWapTradeRemoteServiceImpl implements AlipayWapTradeRemoteService {
	private static final Logger logger = LoggerFactory.getLogger(AlipayQrTradeRemoteServiceImpl.class);
	
	public String doWapOrder(WapOrderReqParam param,AlipayGatewayParam gatewayParam){
		String resp = this.callRemoteService(param, gatewayParam);
		logger.info("resp text for doWapOrder from remote alipay service: {}", resp);
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
}
