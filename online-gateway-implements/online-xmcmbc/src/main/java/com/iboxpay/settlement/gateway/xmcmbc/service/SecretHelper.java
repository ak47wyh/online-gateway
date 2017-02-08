package com.iboxpay.settlement.gateway.xmcmbc.service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcFrontEndConfig;

/**
 * 密钥识别码辅助类
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
public class SecretHelper {
	/**
	 * 生产密钥识别码
	 * 计算方法:MD5(<XML>+SecretKey)
	 * @param xml XML报文
	 * @return    32位长度的16进制字符
	 * @throws PackMessageException
	 */
	public static String genMac(String xml){
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig)context.getFrontEndConfig();
		String secretKey = config.getSecretKey().getVal();
		StringBuilder sb = new StringBuilder(xml);
		sb.append(secretKey);
		String mac = MD5.encode(sb.toString());
		return mac;
	}
	/**
	 * 通过计算生产密钥识别码和返回的匹配，防止报文被篡改
	 * @param returnMac		返回的密钥识别码
	 * @param respXml		返回的xml报文
	 * @return				判断MD5计算生成的密钥识别码和返回的密钥识别码是否相同，想同返回true,否则false
	 * @throws ParseMessageException
	 */
	public static boolean validMatch(String returnMac,String respXml){
		return returnMac.equals(genMac(respXml));
	}
}
