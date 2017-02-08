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
package com.iboxpay.settlement.gateway.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 货币转换工具类
 * @author caolipeng
 * @date 2015年11月6日 下午12:19:13
 * @Version 1.0
 */
public class CurrencyUtil {
	//通道货币单位为分
	public final static BigDecimal MULTI_100 = new BigDecimal("100");
	/**
	 * 将货币转换成以分为单位,0.01---1
	 * @param amount  以元为单位的金额
	 * @param scale   需要保留几位小数
	 * @return 以分为单位的金额，保留0(几)位小数
	 */
	public static String convertToPoint(BigDecimal amount,int scale){
        amount = amount.multiply(MULTI_100);
		BigDecimal convertMoney = amount.setScale(scale,RoundingMode.HALF_DOWN);//交易金额,单位为分
		return convertMoney.toString();
	}
	/**
	 * 将分转化成元
	 * @param amount 以分为单位的金额
	 * @return 以元为单位的金额返回
	 */
	public static String convertToYuan(String amount){
		BigDecimal convert = new BigDecimal(amount);
		convert = convert.divide(MULTI_100);
		return convert.toString();
	}
	
}
