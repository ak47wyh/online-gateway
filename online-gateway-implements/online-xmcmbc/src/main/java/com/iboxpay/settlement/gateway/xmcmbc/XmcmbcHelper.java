/*
 * Copyright (C) 2011-2015 ShenZhen iBOXPAY Information Technology Co.,Ltd.
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
 */
package com.iboxpay.settlement.gateway.xmcmbc;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * The class XmcmbcHelper.
 *
 * Description: 厦门民生代扣辅助类
 *
 * @author: weiyuanhua
 * @since: 2016年2月24日 上午8:56:00 	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public class XmcmbcHelper {

	/**
	 * 判断同行
	 * @param bankName
	 * @return
	 */
	public static boolean isToSameBack(String bankName) {
		if (StringUtils.isBlank(bankName)) {
			return false;
		}

		if (bankName.contains("民生")) {
			return true;
		}

		return false;
	}

	/**
	 * 证件类型转换
	 * @param certType
	 * @return
	 */
	public static String initCertType(String certType) {
		if (!StringUtils.isEmpty(certType)) {
			if (certType.equals("0")) {
				return Constants.CERT_TYPE_ZR01;
			} else if (certType.equals("1")) {
				return Constants.CERT_TYPE_ZR13;
			} /*else if (certType.equals("2")) {
				return Constants.CERT_TYPE_ZR04;
				}*/else if (certType.equals("3")) {
				return Constants.CERT_TYPE_ZR05;
			} else if (certType.equals("4")) {
				return Constants.CERT_TYPE_ZR03;
			} else if (certType.equals("5")) {
				return Constants.CERT_TYPE_ZR02;
			} else if (certType.equals("6")) {
				return Constants.CERT_TYPE_ZR08;
			} /*else if (certType.equals("7")) {
				return Constants.ID_TYPE_107;
				}*/else if (certType.equals("8")) {
				return Constants.CERT_TYPE_ZR24;
			} else if (certType.equals("E")) {
				return Constants.CERT_TYPE_ZR04;
			} else if (certType.equals("Z")) {
				return Constants.CERT_TYPE_ZR20;
			} else {
				return Constants.CERT_TYPE_ZR01;//设置如果没有匹配到设置默认为身份证类型
			}
		} else {
			return Constants.CERT_TYPE_ZR01;//设置如果没有匹配到设置默认为身份证类型
		}
	}

	public static boolean isFail(String code) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("000001", "户名错误");
		map.put("000002", "账号错误");
		map.put("000003", "状态不正常");
		map.put("000004", "账号余额不足");
		map.put("000005", "请求报文不合法");
		map.put("000006", "交易失败");
		map.put("000007", "身份验证不通过");
		map.put("000008", "银联机构号未配置");
		map.put("000009", "帐号种类不对");
		map.put("000010", "非空依赖验证失败");
		map.put("000011", "超出单笔限额");
		map.put("000012", "金额异常");
		map.put("000013", "合作方编号不识别/合作方编号、商户编号不识别");
		map.put("000014", "该笔交易流水已存在");
		map.put("000015", "原交易流水不存在");
		map.put("000016", "账户不允许交易");
		map.put("000017", "证件号码非法，请核实");
		map.put("000018", "通道不支持该笔交易");
		map.put("000019", "商户号未配置,不允许交易");
		map.put("000020", "持卡人身份信息或手机号输入不正确，验证失败");
		map.put("000021", "非白名单账号，交易拒绝");
		map.put("000022", "账号信息重复，请确认");
		map.put("000023", "未在我方进行实名认证或认证不通过，交易拒绝");
		map.put("000024", "超出单日限额");
		map.put("000025", "超出单月限额");
		map.put("000026", "请求渠道失败");
		map.put("000027", "帐号户名不符");
		map.put("000094", "报文解密失败");
		map.put("000095", "报文核签失败");
		map.put("000096", "无可用渠道");
		map.put("000097", "其他错误");
		map.put("000098", "交易超时");

		String c = map.get(code);
		if(c != null) {
			return true;
		}
		else {
			return false;
		}
	}
}
