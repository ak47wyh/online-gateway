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
package com.iboxpay.settlement.gateway.jz.service;

import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.jz.Constants;
/**
 * 封装表单固定参数头部,格式为
 * <p>"merId="+merId+"&"+"transCode="+transCode+"&"+"orderId="+orderId+"&"</p>
 * @author caolipeng
 * @date 2015年8月5日 下午2:32:57
 * @Version 1.0
 */
public class CommonPacker {
	/**
	 * 封装表单参数固定头部
	 * @param merId    商户编号
	 * @param txCode   交易编码
	 * @param orderId  订单号，固定长度20位
	 * @return
	 * @throws PackMessageException
	 */
	public static StringBuffer packHeader(String merId,String txCode,String orderId) 
		throws PackMessageException {
		StringBuffer sb = new StringBuffer();
		sb.append("merId="+merId).append(Constants.ELEMENT_SEPARATOR)
			.append("tradeCode="+txCode).append(Constants.ELEMENT_SEPARATOR)
			.append("orderId="+orderId).append(Constants.ELEMENT_SEPARATOR);
		return sb;
	}
}
