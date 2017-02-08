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
package com.iboxpay.settlement.gateway.jz;

import java.math.BigDecimal;

/**
 * 常量类
 * @author caolipeng
 * @date 2015年8月3日 下午3:26:38
 * @Version 1.0
 */
public class Constants {
	
	/**查询账户余额时候,有基本账户和手续费账户:01-基本账户,02-手续费账户*/
	public static final String BASIC_ACC_TYPE = "01";
	public static final String FEE_ACC_TYPE = "02";
	
	/**交易时间,格式为yyyyMMddHHmmss*/
	public static final String DATE_FORMAT = "yyyyMMddHHmmss";
	
	/**msg字段间分隔符|*/
	public static final String INTERNAL_SEPARATOR = "|";
	
	/**发送报文时候，每个表单元素间连接符为&*/
	public static final String ELEMENT_SEPARATOR = "&";
	
	/**交易状态，00000五个零表示成功*/
	public final static String SUCCESS = "00000";
	/**返回码，1开头的表示失败*/
	public static final String ONE_FAIL = "1";
	
	/**元转分，基数100.00*/
	public static final BigDecimal MULT_100 = new BigDecimal("100.00");
}
