package com.iboxpay.settlement.gateway.kq.service.util;

/**
 * 用于对提交和返回字符串进行包装处理，使其符合SOAP请求格式
 * */
public class StringUtils {
	private final static String preStr = "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>";
	private final static String fixStr = "</soapenv:Body></soapenv:Envelope>";
	
	public static String ReqFormat(String str){
		if(str.length()>0){
			return preStr+str+fixStr;
		}
		return null;
	}
	
	public static String ResFormat(String str){
		if(str.length()>0 && str.contains(fixStr)){
			return str.substring(str.indexOf("<tns:merchant-debit-pki-"),str.indexOf(fixStr));
		}
		return null;
	}
}
