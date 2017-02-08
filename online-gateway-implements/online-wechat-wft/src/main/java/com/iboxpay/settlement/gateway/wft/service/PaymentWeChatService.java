package com.iboxpay.settlement.gateway.wft.service;

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
import com.iboxpay.settlement.gateway.wft.WftFrontEndConfig;
import com.iboxpay.settlement.gateway.wft.service.utils.MD5;
import com.iboxpay.settlement.gateway.wft.service.utils.SignUtils;

/**
 * 微信支付【威富通】-报文组装服务类
 * @author liaoxiongjian
 * @date 2016-03-03 16:38
 */
public class PaymentWeChatService {
	private static Logger logger = LoggerFactory.getLogger(PaymentWeChatService.class);
	
	/**
	 * 微信扫描支付-报文数据初始化
	 * 
	 * @param config
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String, String> initNativePayData(WftFrontEndConfig config,PaymentEntity paymentEntity) {
		String charset=config.getCharset().getVal();
		Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
		// 交易商户号
		String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		    	
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
        SortedMap<String,String> map = new TreeMap<String,String>(mapInit);
        
		map.put("service", config.getNativePayService().getVal());
        map.put("version", config.getVersion().getVal());
        map.put("charset", config.getCharset().getVal());
        map.put("sign_type", config.getSignType().getVal());
        map.put("out_trade_no", paymentEntity.getSeqId());
        
        String productInfo=(String) paymentEntity.getExtProperty("productInfo");
        if(!StringUtils.isEmpty(productInfo)){
        	map.put("body", productInfo);
        }
        map.put("attach", paymentEntity.getRemark());
        
        BigDecimal amount=paymentEntity.getAmount().multiply(new BigDecimal(100));
        map.put("total_fee",amount.toBigInteger().toString());
        map.put("mch_create_ip",config.getMchCreateIp().getVal());
        map.put("mch_id", merchantNo);
        map.put("notify_url", config.getNotifyUrl().getVal());
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
	 * 微信刷卡支付-报文数据初始化
	 * 
	 * @param config
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initMicropayPayData(WftFrontEndConfig config,PaymentEntity paymentEntity) {
		Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
		// 交易商户号
		String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
        SortedMap<String,String> map = new TreeMap<String,String>(mapInit);
        
        map.put("service", config.getMicropayPayService().getVal());
        map.put("version", config.getVersion().getVal());
        map.put("charset", config.getCharset().getVal());
        map.put("sign_type", config.getSignType().getVal());
        
        String authCode=(String) paymentEntity.getExtProperty("authCode");
        String productInfo=(String) paymentEntity.getExtProperty("productInfo");
        map.put("auth_code", authCode);
        map.put("out_trade_no", paymentEntity.getSeqId());
        map.put("body", productInfo);
        map.put("attach", paymentEntity.getRemark());
        BigDecimal amount=paymentEntity.getAmount().multiply(new BigDecimal(100));
        map.put("total_fee",amount.toBigInteger().toString());
        map.put("mch_create_ip",config.getMchCreateIp().getVal());
        map.put("mch_id", merchantNo);
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
	 * @param config
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initQueryDate(WftFrontEndConfig config,PaymentEntity paymentEntity){
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
		SortedMap<String, String> map = new TreeMap<String,String>(mapInit);
		try {			
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易商户号
			String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));

			map.put("service", config.getNativeQueryService().getVal());
			map.put("version", config.getVersion().getVal());
			map.put("charset", config.getCharset().getVal());
			map.put("sign_type", config.getSignType().getVal());
			map.put("out_trade_no", paymentEntity.getSeqId());
			map.put("transaction_id", "");
			map.put("mch_id", merchantNo);
			map.put("nonce_str", String.valueOf(new Date().getTime()));
			Map<String, String> params = SignUtils.paraFilter(map);
			StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
			SignUtils.buildPayParams(buf, params, false);
			String preStr = buf.toString();
			String sign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8");
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
	public static SortedMap<String,String> initRefundData(WftFrontEndConfig config,PaymentEntity paymentEntity){
		HashMap<String, String> mapInit = new HashMap<String, String>();
		SortedMap<String, String> map = new TreeMap<String, String>(mapInit);
		
		
		try {
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易商户号
			String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			
			//组装报文数据   
			map.put("service", config.getNativieRefundService().getVal());
			map.put("version", config.getVersion().getVal());
			map.put("charset", config.getCharset().getVal());
			map.put("sign_type", config.getSignType().getVal());
			map.put("out_trade_no", paymentEntity.getSeqId());
			
			String  outRefundNo=(String) paymentEntity.getExtProperty("outRefundNo");
			map.put("out_refund_no", outRefundNo);
			BigDecimal amount=paymentEntity.getAmount().multiply(new BigDecimal(100));
			map.put("total_fee",amount.toBigInteger().toString());
			map.put("refund_fee", amount.toBigInteger().toString());
			map.put("refund_channel", "ORIGINAL");
			map.put("mch_id", merchantNo);
			map.put("op_user_id", merchantNo);//操作员
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
	 * 微信退款-报文组装
	 * 
	 * @param config
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initRefundQueryData(WftFrontEndConfig config,PaymentEntity paymentEntity){
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
        SortedMap<String,String> map = new TreeMap<String,String>(mapInit);

		try {
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易商户号
			String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			
			// 数据组装转换
			map.put("service", config.getRefundQueryService().getVal());
			map.put("version", config.getVersion().getVal());
			map.put("charset", config.getCharset().getVal());
			map.put("sign_type", config.getSignType().getVal());
			map.put("out_trade_no", paymentEntity.getSeqId());
			map.put("transaction_id", "");
			map.put("mch_id", merchantNo);
			map.put("nonce_str", String.valueOf(new Date().getTime()));
			Map<String, String> params = SignUtils.paraFilter(map);
			StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
			SignUtils.buildPayParams(buf, params, false);
			String preStr = buf.toString();
			String sign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8");
			map.put("sign", sign);
			
		} catch (JsonParseException e) {
			logger.error("initRefundQueryData 组装报文异常:"+e);
		} catch (JsonMappingException e) {
			logger.error("initRefundQueryData 组装报文异常:"+e);
		} catch (IOException e) {
			logger.error("initRefundQueryData 组装报文异常:"+e);
		}
		
		return map;
	}
	
	
	/**
	 * 微信关闭-报文组装
	 * 
	 * @param config
	 *            前置机
	 * @param accountExt
	 *            扩展账户信息
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initCloseDate(WftFrontEndConfig config,PaymentEntity paymentEntity){
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
        SortedMap<String,String> map = new TreeMap<String,String>(mapInit);
        
		try {
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易商户号
			String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));

			// 数据组装
			map.put("service", config.getCloseService().getVal());
			map.put("version", config.getVersion().getVal());
			map.put("charset", config.getCharset().getVal());
			map.put("sign_type", config.getSignType().getVal());
			map.put("out_trade_no", paymentEntity.getSeqId());
			map.put("mch_id", merchantNo);
			map.put("nonce_str", String.valueOf(new Date().getTime()));
			Map<String, String> params = SignUtils.paraFilter(map);
			StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
			SignUtils.buildPayParams(buf, params, false);
			String preStr = buf.toString();
			String sign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8");
			map.put("sign", sign);
		} catch (JsonParseException e) {
			logger.error("initCloseDate 组装报文异常:"+e);
		} catch (JsonMappingException e) {
			logger.error("initCloseDate 组装报文异常:"+e);
		} catch (IOException e) {
			logger.error("initCloseDate 组装报文异常:"+e);
		}
		
		return map;
	}
	
	/**
	 * 微信冲正/撤销-报文组装
	 * 
	 * @param config
	 *            前置机
	 * @param paymentEntity
	 *            支付信息
	 * @return
	 */
	public static SortedMap<String,String> initReverseData(WftFrontEndConfig config,PaymentEntity paymentEntity){
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
        SortedMap<String,String> map = new TreeMap<String,String>(mapInit);

		try {
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易商户号
			String merchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			// 数据组装转换
			map.put("service", config.getMicropayReverseService().getVal());
			map.put("version", config.getVersion().getVal());
			map.put("charset", config.getCharset().getVal());
			map.put("sign_type", config.getSignType().getVal());
			map.put("out_trade_no", paymentEntity.getSeqId());
			map.put("mch_id", merchantNo);
			map.put("nonce_str", String.valueOf(new Date().getTime()));
			Map<String, String> params = SignUtils.paraFilter(map);
			StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
			SignUtils.buildPayParams(buf, params, false);
			String preStr = buf.toString();
			String sign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8");
			map.put("sign", sign);
		} catch (JsonParseException e) {
			logger.error("initReverseData 组装报文异常:"+e);
		} catch (JsonMappingException e) {
			logger.error("initReverseData 组装报文异常:"+e);
		} catch (IOException e) {
			logger.error("initReverseData 组装报文异常:"+e);
		}
		return map;
	}
	
	
	/**
	 * 构造反馈成功扩展结果信息
	 * @param resultMap
	 * @return 
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
		// 威富通订单号
		String transactionId=resultMap.get("transaction_id");
		
		Map<String,Object> extParam=new HashMap<String,Object>();
		String codeUrl = (String) paymentEntity.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl);
		if(StringUtils.isEmpty(codeUrl)){
			extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl, codeUrl);
		}
		String codeImgUrl = (String) paymentEntity.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl);
		if(StringUtils.isEmpty(codeImgUrl)){
			extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl, codeImgUrl);
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
	
}
