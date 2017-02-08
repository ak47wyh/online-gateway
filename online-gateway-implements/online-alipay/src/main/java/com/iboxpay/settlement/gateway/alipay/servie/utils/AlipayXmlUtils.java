/*
 * Copyright (C) 2011-2013 ShenZhen iBOXPAY Information Technology Co.,Ltd.
 * 
 * All right reserved.
 * 
 * This software is the confidential and proprietary
 * information of iBoxPay Company of China. 
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement 
 * you entered into with iBoxpay inc.
 *
 * $Id: XmlHelper.java 4688 2013-07-17 07:39:39Z deli $
 * 
 * Create on 2012-8-7
 * 
 * Description: 
 *
 */

package com.iboxpay.settlement.gateway.alipay.servie.utils;

import com.iboxpay.settlement.gateway.alipay.servie.model.BaseRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.CancelTradeRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.PreCreateRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.QueryStatusRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.RefundTradeRespParam;

/**
 * 解析支付宝接口返回文本
 * @author Jim Yang (oraclebone@gmail.com)
 *
 */
public class AlipayXmlUtils {

	/**
	 * 解析预下单接口返回文本
	 * @param xmlStr
	 * @return
	 */
	public static PreCreateRespParam parsePreCreate(String xmlStr) {
		XmlDomParser p = new XmlDomParser(xmlStr);
		PreCreateRespParam param = new PreCreateRespParam();
		
		setCommonProperties(param, p);
		
		param.setOut_trade_no(p.getValue("alipay.response.alipay.out_trade_no"));
		param.setBig_pic_url(p.getValue("alipay.response.alipay.big_pic_url"));
		param.setPic_url(p.getValue("alipay.response.alipay.pic_url"));
		param.setSmall_pic_url(p.getValue("alipay.response.alipay.small_pic_url"));
		param.setQr_code(p.getValue("alipay.response.alipay.qr_code"));
		param.setVoucher_type(p.getValue("alipay.response.alipay.voucher_type"));
		param.setTrade_no(p.getValue("alipay.response.alipay.trade_no"));
		
		
		return param;
	}
	
	/**
	 * 解析收单状态查询接口返回文本
	 * @param xmlStr
	 * @return
	 */
	public static QueryStatusRespParam parseQueryStatus(String xmlStr) {
		XmlDomParser p = new XmlDomParser(xmlStr);
		QueryStatusRespParam param = new QueryStatusRespParam();
		
		setCommonProperties(param, p);
		
		param.setOut_trade_no(p.getValue("alipay.response.alipay.out_trade_no"));
		param.setTrade_no(p.getValue("alipay.response.alipay.trade_no"));
		param.setBuyer_user_id(p.getValue("alipay.response.alipay.buyer_user_id"));
		param.setBuyer_logon_id(p.getValue("alipay.response.alipay.buyer_logon_id"));
		param.setPartner(p.getValue("alipay.response.alipay.partner"));
		param.setTrade_status(p.getValue("alipay.response.alipay.trade_status"));
		
		
		return param;
	}
	
	/**
	 * 解析收单撤销接口返回文本
	 * @param xmlStr
	 * @return
	 */
	public static CancelTradeRespParam parseCancelTrade(String xmlStr) {
		XmlDomParser p = new XmlDomParser(xmlStr);
		CancelTradeRespParam param = new CancelTradeRespParam();
		
		setCommonProperties(param, p);
		
		param.setOut_trade_no(p.getValue("alipay.response.alipay.out_trade_no"));
		param.setTrade_no(p.getValue("alipay.response.alipay.trade_no"));
		param.setRetry_flag(p.getValue("alipay.response.alipay.retry_flag"));
		
		
		return param;
	}
	
	/**
	 * 解析退款接口返回文本
	 * @param xmlStr
	 * @return
	 */
	public static RefundTradeRespParam parseRefundTrade(String xmlStr) {
		XmlDomParser p = new XmlDomParser(xmlStr);
		RefundTradeRespParam param = new RefundTradeRespParam();
		
		setCommonProperties(param, p);
		
		param.setOut_trade_no(p.getValue("alipay.response.alipay.out_trade_no"));
		param.setTrade_no(p.getValue("alipay.response.alipay.trade_no"));
		param.setBuyer_user_id(p.getValue("alipay.response.alipay.buyer_user_id"));
		param.setBuyer_logon_id(p.getValue("alipay.response.alipay.buyer_logon_id"));
		param.setFund_change(p.getValue("alipay.response.alipay.fund_change"));
	
		return param;
	}
	
	/**
	 * 解析通用返回属性
	 * @param param
	 * @param p
	 */
	private static void setCommonProperties(BaseRespParam param, XmlDomParser p){
		param.setIs_success(p.getValue("alipay.is_success"));
		param.setSign(p.getValue("alipay.sign"));
		param.setSign_type(p.getValue("alipay.sign_type"));
		param.setError(p.getValue("alipay.error"));
		param.setDetail_error_code(p.getValue("alipay.response.alipay.detail_error_code"));
		param.setDetail_error_des(p.getValue("alipay.response.alipay.detail_error_des"));
		param.setResult_code(p.getValue("alipay.response.alipay.result_code"));
	}

	public static void main(String[] args){
		
		String xml = "<alipay><is_success>T</is_success><request><param name=\"body\">珠宝饰品</param><param name=\"operator_id\">8888</param><param name=\"subject\">测试</param><param name=\"sign_type\">MD5</param></request></alipay>";
		
		XmlDomParser parser = new XmlDomParser(xml);
		System.out.println(parser.getValue("alipay.is_success"));
		System.out.println(parser.getValue("alipay.request.param@name"));
		System.out.println(parser.getValue("alipay.request.param@name#body"));
		System.out.println(parser.getValue("alipay.request.param@name#subject"));
		System.out.println(parser.getValue("alipay.request.param@name#sign_type"));
		System.out.println();
		System.out.println(parser.getValue("alipay.xxx"));
		System.out.println(parser.getValue("alipay.xxx.param@name"));
		System.out.println(parser.getValue("alipay.xx.param@name#body"));
		System.out.println(parser.getValue("alipay.request.param@name#xxx"));
		System.out.println(parser.getValue("alipay.request.param@yyy#sign_type"));
		System.out.println();
	}


}
