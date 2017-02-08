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
package com.iboxpay.settlement.gateway.jzdsf.service;

import org.dom4j.Element;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.util.DomUtil;

/**
 * XML 头部内容解析工具类
 *
 * @author: fengweichao
 *	
 * @2016年2月23日  @上午10:49:00
 */
public class CommonParser {

    public static HeadInfo parseHead(Element root) throws ParseMessageException {
        Element head = root.element("Head");
        Element out = root.element("Out");
        //查询状态0-失败/1-成功，失败时，无out 信息
        String resFlag = DomUtil.getTextTrimNotNull(head, "ResFlag");
        HeadInfo headInfo = null;
        if ("1".equals(resFlag)) {//成功
            String retCode = DomUtil.getTextTrimNotNull(out, "RetCode");
            String retMsg = DomUtil.getTextTrimNotNull(out, "RetMsg");
            String remark = DomUtil.getTextTrimNotNull(out, "Remark");
            headInfo = new HeadInfo(resFlag, retCode, retMsg + "(" + remark + ")");
        } else if ("0".equals(resFlag)) {
            String errorCode = DomUtil.getTextTrimNotNull(head, "ErrorCode");
            String errorMsg = DomUtil.getTextTrimNotNull(head, "ErrorMsg");
            headInfo = new HeadInfo(resFlag, errorCode, errorMsg);
        }
        return headInfo;
    }

    public static class HeadInfo {

        public final String CODE;
        public final String MESSAGE;
        public final boolean SUCCESS;

        public HeadInfo(String resFlag, String errorCode, String errorMsg) {
            this.SUCCESS = "1".equals(resFlag);
            this.CODE = errorCode;
            this.MESSAGE = errorMsg;
        }
    }
    
}
