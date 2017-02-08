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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.common.json.MapperUtils;
import com.iboxpay.common.utils.OkHttpUtils;
import com.iboxpay.settlement.gateway.common.cache.remote.MemcachedService;

/**
 * 
 * 从微信获取jsticket存在memcached里面
 *
 * @author: yinchao
 * @since: 2016年1月9日    
 *
 */
@Service("weixinJsTicketInfoService")
public class WeixinJsTicketInfoServiceImpl implements WeixinJsTicketInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeixinJsTicketInfoServiceImpl.class);

    @Resource
    private MemcachedService memcachedService;
    
    public String getJsTicketInfo(String appId,String appSecret) throws Exception {
//        
//        String weixinOfficialAppId = "wx9cc8a35182321de1";
//        String weixinOfficialAppSecret = "31513cf88584b9e51255787823d1fa35";
        
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("grant_type", "client_credential");
        paramMap.put("appid", appId);
        paramMap.put("secret", appSecret);
        String getTokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
        Map<String, Object> tokenResultMap = OkHttpUtils.httpClientGetReturnAsMap(getTokenUrl, paramMap, 50);
        LOGGER.info("-------the tokenResultMap:{}", MapperUtils.toJson(tokenResultMap));
        String accessToken = (String) tokenResultMap.get("access_token");
        if (null != accessToken) {
            int tokenExpiresIn = 7200;
            if (null != tokenResultMap.get("expires_in")) {
                tokenExpiresIn = Integer.valueOf(String.valueOf(tokenResultMap.get("expires_in")));
            }
            memcachedService.setWithType("wxOfficalAccessToken", accessToken);
            paramMap = new HashMap<String, Object>();
            paramMap.put("access_token", accessToken);
            paramMap.put("type", "jsapi");
            String getTicketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
            Map<String, Object> ticketResultMap = OkHttpUtils.httpClientGetReturnAsMap(getTicketUrl, paramMap, 50);
            LOGGER.info("-------the ticketResultMap:{}", MapperUtils.toJson(ticketResultMap));
            String ticket = (String) ticketResultMap.get("ticket");
            if (null != ticket) {
                int ticketExpiresIn = 7200;
                if (null != ticketResultMap.get("expires_in")) {
                    ticketExpiresIn = Integer.valueOf(String.valueOf(ticketResultMap.get("expires_in")));
                };
                
                JsTicket jsticket =new JsTicket();
                jsticket.setTicket(ticket);
                jsticket.setCreateTime(new Date());
                jsticket.setExpiresIn(ticketExpiresIn);
                memcachedService.set("wxOfficalJsTicket", jsticket);
                return ticket;
            } else {
                LOGGER.info("-- get jsapi ticket fail --");
            }
        } else {
            LOGGER.info("-- get access_token fail --");
        }
        return null;
    }
}
