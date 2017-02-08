/*
 * Copyright (C) 2011-2016 ShenZhen iBOXPAY Information Technology Co.,Ltd.
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
package com.iboxpay.settlement.gateway.wechat.service.sign.utils;

/**
 * 
 *
 * 从微信获取jsticket存在redis里面
 *
 * @author: wangyanhui
 * @since: 2016年1月9日	
 *
 */
public interface WeixinJsTicketInfoService {

    /**
     * 从微信获取jsticket
     */
    String getJsTicketInfo(String appId,String appSecret) throws Exception;
}
