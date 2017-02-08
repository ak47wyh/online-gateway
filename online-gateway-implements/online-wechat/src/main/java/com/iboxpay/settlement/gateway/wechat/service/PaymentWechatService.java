package com.iboxpay.settlement.gateway.wechat.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.wechat.WechatFrontEndConfig;
import com.iboxpay.settlement.gateway.wechat.service.utils.MD5;
import com.iboxpay.settlement.gateway.wechat.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wechat.service.utils.WechatUtils;

/**
 * 微信支付-报文组装服务类
 * @author liaoxiongjian
 * @date 2016-03-05 16:38
 */
public class PaymentWechatService {
	private static Logger logger = LoggerFactory.getLogger(PaymentWechatService.class);
	/**
	 * 微信扫描支付-报文组装
	 * 
	 * @param wechatConfig
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String, String> initNativePayData(WechatFrontEndConfig wechatConfig,PaymentEntity paymentEntity) {
		String charset=wechatConfig.getCharset().getVal();		
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
        SortedMap<String,String> map = new TreeMap<String,String>(mapInit);
		Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
		// 交易商户号
		String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		// 子商户号
		String merchantSubNo=String.valueOf(merchantMap.get("payMerchantSubNo"));
		// 公众账号ID
		String appId = String.valueOf(merchantMap.get("appId"));
		String subAppId =String.valueOf(merchantMap.get("subAppId"));
		if(!StringUtils.isEmpty(subAppId)&&!subAppId.equals("null")){
			map.put("sub_appid", subAppId);
		}
		// 数据组装转换
        map.put("appid", appId);        
        map.put("mch_id", merchantNo);
        map.put("sub_mch_id",merchantSubNo); 

        map.put("out_trade_no", paymentEntity.getSeqId());
        String productInfo=(String) paymentEntity.getExtProperty("productInfo");
        map.put("body", productInfo);
        map.put("attach", paymentEntity.getRemark());
        
        BigDecimal amount=paymentEntity.getAmount().multiply(new BigDecimal(100));
        map.put("total_fee",amount.toBigInteger().toString());
        map.put("spbill_create_ip",wechatConfig.getMchCreateIp().getVal());
        map.put("trade_type", "NATIVE");
        map.put("notify_url", wechatConfig.getNotifyUrl().getVal());
        map.put("nonce_str", String.valueOf(new Date().getTime()));
            
        // 签名
		Map<String, String> params = SignUtils.paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.sign(preStr, "&key=" + merchantKey, charset);
		map.put("sign", sign);
		return map;
	}
	
	/**
	 * 微信刷卡支付-报文组装
	 * 
	 * @param wechatConfig
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initMicropayPayData(WechatFrontEndConfig wechatConfig,PaymentEntity paymentEntity) {
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
        SortedMap<String,String> map = new TreeMap<String,String>(mapInit);
		Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
		// 交易商户号
		String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		// 子商户号
		String merchantSubNo=String.valueOf(merchantMap.get("payMerchantSubNo"));
		// 公众账号ID
		String appId = String.valueOf(merchantMap.get("appId"));
		
		// 数据组装转换		
        map.put("appid", appId);
        map.put("mch_id", merchantNo);
        map.put("sub_mch_id", merchantSubNo);         
        String authCode=(String) paymentEntity.getExtProperty("authCode");
        String productInfo=(String) paymentEntity.getExtProperty("productInfo");
        map.put("out_trade_no", paymentEntity.getSeqId());
        BigDecimal amount=paymentEntity.getAmount().multiply(new BigDecimal(100));
        map.put("total_fee",amount.toBigInteger().toString());        
        map.put("body", productInfo);
        map.put("attach", paymentEntity.getRemark());
        map.put("spbill_create_ip",wechatConfig.getMchCreateIp().getVal());
        map.put("auth_code", authCode);
        
        
        map.put("nonce_str", String.valueOf(new Date().getTime()));    
        // 签名
		Map<String, String> params = SignUtils.paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8");
		map.put("sign", sign);
		return map;
	}
	
	
	/**
	 * 微信公众号支付-报文组装
	 * 
	 * @param wechatConfig
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initJsapiPayData(WechatFrontEndConfig wechatConfig,PaymentEntity paymentEntity) {
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
        SortedMap<String,String> map = new TreeMap<String,String>(mapInit);
		Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
		// 交易商户号
		String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		// 子商户号
		String merchantSubNo=String.valueOf(merchantMap.get("payMerchantSubNo"));
		// 公众账号ID
		String appId = String.valueOf(merchantMap.get("appId"));
		String subAppId = String.valueOf(merchantMap.get("subAppId"));
		if(!StringUtils.isEmpty(subAppId)&&!subAppId.equals("null")){
			map.put("sub_appid", subAppId);
		}
		
		// 数据组装转换		
        map.put("appid", appId);
        map.put("mch_id", merchantNo);
        map.put("sub_mch_id", merchantSubNo);         
        String openid=(String) paymentEntity.getExtProperty("openid");
        String subopenid=(String) paymentEntity.getExtProperty("subopenid");
        String productInfo=(String) paymentEntity.getExtProperty("productInfo");
        map.put("out_trade_no", paymentEntity.getSeqId());
        BigDecimal amount=paymentEntity.getAmount().multiply(new BigDecimal(100));
        map.put("total_fee",amount.toBigInteger().toString());        
        map.put("body", productInfo);
        map.put("attach", paymentEntity.getRemark());
        map.put("spbill_create_ip",wechatConfig.getMchCreateIp().getVal());
        map.put("notify_url", wechatConfig.getNotifyUrl().getVal());
        map.put("trade_type", "JSAPI");
        map.put("openid", openid);
        map.put("sub_openid", subopenid);
        
        
        map.put("nonce_str", String.valueOf(new Date().getTime()));    
        // 签名
		Map<String, String> params = SignUtils.paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8");
		map.put("sign", sign);
		return map;
	}
	
	/**
	 * 微信状态查询-报文组装
	 * 
	 * @param wechatConfig
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initQueryDate(WechatFrontEndConfig wechatConfig,PaymentEntity paymentEntity){
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
        SortedMap<String,String> map = new TreeMap<String,String>(mapInit);
        
		try {
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易商户号
			String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
			// 子商户号
			String merchantSubNo=String.valueOf(merchantMap.get("payMerchantSubNo"));
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			// 公众账号ID
			String appId = String.valueOf(merchantMap.get("appId"));
			String subAppId = String.valueOf(merchantMap.get("subAppId"));
			if(!StringUtils.isEmpty(subAppId)&&!subAppId.equals("null")){
				map.put("sub_appid", subAppId);
			}
			// 数据组装转换
			map.put("appid", appId);
			map.put("mch_id", merchantNo);
			map.put("sub_mch_id", merchantSubNo); 
			map.put("out_trade_no", paymentEntity.getSeqId());
			map.put("transaction_id", "");
			map.put("nonce_str", String.valueOf(new Date().getTime()));
			Map<String, String> params = SignUtils.paraFilter(map);
			StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
			SignUtils.buildPayParams(buf, params, false);
			String preStr = buf.toString();
			String sign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8").toUpperCase();
			map.put("sign", sign);
			
		} catch (JsonParseException e) {
			logger.error("initQueryDate 组装报文异常:"+e);
		} catch (JsonMappingException e) {
			logger.error("initQueryDate 组装报文异常:"+e);
		} catch (IOException e) {
			logger.error("initQueryDate 组装报文异常:"+e);
		}
				
		return map;
	}
	
	
	/**
	 * 微信退款-报文组装
	 * 
	 * @param config
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initRefundData(WechatFrontEndConfig wechatConfig,PaymentEntity paymentEntity){
		HashMap<String, String> mapInit = new HashMap<String, String>();
		SortedMap<String, String> map = new TreeMap<String, String>(mapInit);
		
		try {
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易商户号
			String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
			// 子商户号
			String merchantSubNo=String.valueOf(merchantMap.get("payMerchantSubNo"));
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			// 公众账号ID
			String appId = String.valueOf(merchantMap.get("appId"));
			String subAppId = String.valueOf(merchantMap.get("subAppId"));
			if(!StringUtils.isEmpty(subAppId)&&!subAppId.equals("null")){
				map.put("sub_appid", subAppId);
			}
			//组装报文数据   
			map.put("appid", appId);
			map.put("mch_id", merchantNo);
			map.put("sub_mch_id", merchantSubNo); 
			map.put("out_trade_no", paymentEntity.getSeqId());
			
			String  outRefundNo=(String) paymentEntity.getExtProperty("outRefundNo");
			map.put("out_refund_no", outRefundNo);
			BigDecimal amount=paymentEntity.getAmount().multiply(new BigDecimal(100));
			map.put("total_fee",amount.toBigInteger().toString());
			map.put("refund_fee", amount.toBigInteger().toString());
			map.put("refund_channel", "ORIGINAL");
			map.put("op_user_id",merchantNo);//操作员
			map.put("nonce_str", String.valueOf(new Date().getTime()));

			Map<String,String> params = SignUtils.paraFilter(map);
			StringBuilder buf = new StringBuilder((params.size() +1) * 10);
			SignUtils.buildPayParams(buf,params,false);
			String preStr = buf.toString();
			String sign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8");
			map.put("sign", sign);
		} catch (JsonParseException e) {
			logger.error("initRefundData 组装报文异常:"+e);
		} catch (JsonMappingException e) {
			logger.error("initRefundData 组装报文异常:"+e);
		} catch (IOException e) {
			logger.error("initRefundData 组装报文异常:"+e);
		}
        
        return map;
	}
	
	
	/**
	 * 微信退款/冲正/撤销-报文组装
	 * 
	 * @param wechatConfig
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initCommonQueryData(WechatFrontEndConfig wechatConfig,PaymentEntity paymentEntity){
		HashMap<String, String> mapInit = new HashMap<String, String>();
		SortedMap<String, String> map = new TreeMap<String, String>(mapInit);

		try {
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易商户号
			String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
			// 子商户号
			String merchantSubNo=String.valueOf(merchantMap.get("payMerchantSubNo"));
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			// 公众账号ID
			String appId = String.valueOf(merchantMap.get("appId"));
			String subAppId = String.valueOf(merchantMap.get("subAppId"));
			if(!StringUtils.isEmpty(subAppId)&&!subAppId.equals("null")){
				map.put("sub_appid", subAppId);
			}
			//组装报文数据   
			map.put("appid", appId);
			map.put("mch_id", merchantNo);
			map.put("sub_mch_id", merchantSubNo); 
			map.put("out_trade_no", paymentEntity.getSeqId());
			map.put("nonce_str", String.valueOf(new Date().getTime()));

			Map<String,String> params = SignUtils.paraFilter(map);
			StringBuilder buf = new StringBuilder((params.size() +1) * 10);
			SignUtils.buildPayParams(buf,params,false);
			String preStr = buf.toString();
			String sign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8");
			map.put("sign", sign);
		} catch (JsonParseException e) {
			logger.error("initCommonQueryData 组装报文异常:"+e);
		} catch (JsonMappingException e) {
			logger.error("initCommonQueryData 组装报文异常:"+e);
		} catch (IOException e) {
			logger.error("initCommonQueryData 组装报文异常:"+e);
		}
		
		return map;
	}	
	
	/**
	 * 构造反馈成功扩展结果信息
	 * @param resultMap
	 * @return Json 格式的扩展数据
	 */
	public static synchronized String initCallbackExtProperties(Map<String, String> resultMap,PaymentEntity paymentEntity) {
		// 用户标识
		String openid=resultMap.get("openid");
		// 用户子标识
		String subOpenid=resultMap.get("sub_openid");
		// 是否关注公众账号
		String isSubscribe=resultMap.get("is_subscribe");
		// 是否关注子公众账号
		String subIsSubscribe=resultMap.get("sub_is_subscribe");
		// 付款银行
		String bankType=resultMap.get("bank_type");
		// 货币类型
		String feeType=resultMap.get("fee_type");
		// 微信支付订单号
		String transactionId=resultMap.get("transaction_id");
		
		Map<String,Object> extParam=new HashMap<String,Object>();
		String codeUrl = (String) paymentEntity.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl);
		if(!StringUtils.isEmpty(codeUrl)){
			extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl, codeUrl);
		}
		String codeImgUrl = (String) paymentEntity.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl);
		if(!StringUtils.isEmpty(codeImgUrl)){
			extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl, codeImgUrl);
		}
		String appId = (String) paymentEntity.getCallbackExtProperties("appId");
		if(!StringUtils.isEmpty(appId)){
			extParam.put("appId", appId);
		}
		String timeStamp = (String) paymentEntity.getCallbackExtProperties("timeStamp");
		if(!StringUtils.isEmpty(timeStamp)){
			extParam.put("timeStamp", timeStamp);
		}
		String nonceStr = (String) paymentEntity.getCallbackExtProperties("nonceStr");
		if(!StringUtils.isEmpty(nonceStr)){
			extParam.put("nonceStr", nonceStr);
		}
		String signType = (String) paymentEntity.getCallbackExtProperties("signType");
		if(!StringUtils.isEmpty(signType)){
			extParam.put("signType", signType);
		}
		String paySign = (String) paymentEntity.getCallbackExtProperties("paySign");
		if(!StringUtils.isEmpty(paySign)){
			extParam.put("paySign", paySign);
		}
		String prepayId = (String) paymentEntity.getCallbackExtProperties("prepayId");
		if(!StringUtils.isEmpty(prepayId)){
			extParam.put("prepayId", prepayId);
		}
		
		
		
				
		extParam.put("openid", openid);
		extParam.put("subOpenid", subOpenid);
		extParam.put("isSubscribe", isSubscribe);
		extParam.put("subIsSubscribe", subIsSubscribe);
		extParam.put("bankType", bankType);
		extParam.put("feeType", feeType);
		extParam.put("transactionId", transactionId);
		String callbackExtProperties=JsonUtil.toJson(extParam);
		return callbackExtProperties;
	}
	
	
	/**
	 * 组装微信公众号支付扩展返回参数信息
	 * 
	 * @param appId
	 *            公众号
	 * @param appSecret
	 *            商户秘钥
	 * @param prepayId
	 *            预下单编号
	 * @param config
	 *            前置机
	 * @return
	 */
	public static synchronized String initJsApiCallbackExtProperties(String appId, String appSecret, String prepayId, WechatFrontEndConfig config) {
		Map<String, String> extParam = new HashMap<String, String>();
		String timeStamp =String.valueOf(new Date().getTime());
		String nonceStr= String.valueOf(new Date().getTime());
		extParam.put("appId", appId);
		extParam.put("timeStamp", timeStamp);
		extParam.put("nonceStr", nonceStr);
		String packageStr="prepay_id="+prepayId;
		extParam.put("packageExt", packageStr);
		extParam.put("signType", "MD5");
		String paySign = WechatUtils.createPaySign(appId,appSecret,prepayId, timeStamp, nonceStr);
		extParam.put("paySign", paySign);
		
		extParam.put("prepayId", prepayId);
		String callbackExtProperties = JsonUtil.toJson(extParam);
		return callbackExtProperties;
	}


}

