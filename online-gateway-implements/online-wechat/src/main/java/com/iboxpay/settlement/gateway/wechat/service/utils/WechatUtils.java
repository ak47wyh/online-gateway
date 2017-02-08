package com.iboxpay.settlement.gateway.wechat.service.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.iboxpay.common.utils.OkHttpUtils;
import com.iboxpay.settlement.gateway.common.util.PropertyReader;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

public class WechatUtils {

	private static Logger logger = LoggerFactory.getLogger(WechatUtils.class);
	/**
	 * 根据code获取accessToken
	 * 
	 * 网页授权接口微信服务器返回的数据，返回样例如下
	 * {
	 *  "access_token":"ACCESS_TOKEN",
	 *  "expires_in":7200,
	 *  "refresh_token":"REFRESH_TOKEN",
	 *  "openid":"OPENID",
	 *  "scope":"SCOPE",
	 *  "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
	 * }
	 * 其中access_token可用于获取共享收货地址
	 * openid是微信支付jsapi支付接口必须的参数
     *
     *
     * @param code
	 * @return 
	 * @throws Exception
	 */
	public static String getAccessToken(String code) throws Exception{
		PropertyReader reader = PropertyReader.getInstance();
    	String url = reader.getPropertyValue("/wechat.properties", "wechat.sns.oauth2.url");
    	url=url+"/access_token";
    	String appid = reader.getPropertyValue("/wechat.properties", "wechat.appid");
    	String secret = reader.getPropertyValue("/wechat.properties", "wechat.appsecret");
		
		Map<String,Object> param=new HashMap<String,Object>();
		param.put("appid", appid);
		param.put("secret", secret);
		param.put("code", code);
		param.put("grant_type", "authorization_code");
		String result = OkHttpUtils.httpClientGetReturnAsString(url, param, 60);
		
		logger.info("getAccessToken 数据返回："+result);
		return result;
	}
	
	
	/**
	 * 刷新access_token
	 * @param appid
	 * @param refreshToken
	 * @throws Exception 
	 * 
	 * 服务器返回的数据，返回样例如下
	 * {
		   "access_token":"ACCESS_TOKEN",
		   "expires_in":7200,
		   "refresh_token":"REFRESH_TOKEN",
		   "openid":"OPENID",
		   "scope":"SCOPE"
      }
	 */
	public static String refreshAccessToken(String appid,String refreshToken) throws Exception{
		PropertyReader reader = PropertyReader.getInstance();
    	String url = reader.getPropertyValue("/wechat.properties", "wechat.sns.oauth2.url");
    	url=url+"/refresh_token";

		Map<String,Object> param=new HashMap<String,Object>();
		param.put("appid", appid);
		param.put("grant_type", "refresh_token");
		param.put("refresh_token", refreshToken);
	    String result = OkHttpUtils.httpClientGetReturnAsString(url, param, 60);
	    
	    logger.info("refreshAccessToken 数据返回："+result);
	    return result;
	}
	
	/**
	 * 拉取用户信息
	 * @throws Exception 
	 */
	public static String pullUserInfo(String accessToken, String openid,String lang) throws Exception{
		PropertyReader reader = PropertyReader.getInstance();
    	String url = reader.getPropertyValue("/wechat.properties", "wechat.sns.oauth2.url");
    	url=url+"/userinfo";
		
		Map<String,Object> param=new HashMap<String,Object>();
		param.put("access_token", accessToken);
		param.put("openid", openid);
		if(!StringUtils.isEmpty(lang)&&!lang.equals("null")){
			param.put("lang", lang);
		}
	    String result = OkHttpUtils.httpClientGetReturnAsString(url, param, 60);
	    
	    logger.info("pullUserInfo 数据返回："+result);
	    return result;
	}
	
	
	/**
	 * 微信公共签名入口
	 * 
	 * @param appId
	 *            公众号
	 * @param merchantKey
	 *            商户密钥
	 * @param timeStamp
	 *            时间戳
	 * @param nonceStr
	 *            随机串
	 * @return 加密字符串
	 */
	public static String createSignature(String url ,String appId, String merchantKey , String timeStamp, String nonceStr) {
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
	    SortedMap<String,String> param = new TreeMap<String,String>(mapInit);
	    param.put("url", url);
		param.put("appId", appId);
		param.put("timeStamp", timeStamp);
		param.put("nonceStr", nonceStr);
		param.put("signType", "MD5");
		// 签名
		Map<String, String>  params= SignUtils.paraFilter(param);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String signature = MD5.sign(preStr, "&key=" + merchantKey, "utf-8").toUpperCase();
		return signature;
	}
	
	/**
	 * 微信支付签名
	 * 
	 * @param appId
	 *            公众号
	 * @param merchantKey
	 *            商户秘钥
	 * @param prepayId
	 *            预下单标示
	 * @param timeStamp
	 *            时间戳
	 * @param nonceStr
	 *            随机串
	 * @return 加密字符串
	 */
	public static String createPaySign(String appId, String merchantKey,String prepayId,String timeStamp, String nonceStr) {
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
	    SortedMap<String,String> extParam = new TreeMap<String,String>(mapInit);
		extParam.put("appId", appId);
		extParam.put("timeStamp", timeStamp);
		extParam.put("nonceStr", nonceStr);
		String packageStr="prepay_id="+prepayId;
		extParam.put("package", packageStr);
		extParam.put("signType", "MD5");
		// 签名
		Map<String, String> params = SignUtils.paraFilter(extParam);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String paySign = MD5.sign(preStr, "&key=" + merchantKey, "utf-8").toUpperCase();
		return paySign;
	}
}
