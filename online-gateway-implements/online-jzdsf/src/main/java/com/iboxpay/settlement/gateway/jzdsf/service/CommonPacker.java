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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.util.DomUtil;
import com.iboxpay.settlement.gateway.jzdsf.JZDSFFrontEndConfig;

/**
 * XML 头部内容组装工具类
 *
 * @author: fengweichao
 *	
 * @2016年2月23日  @上午10:47:39
 */
public class CommonPacker {

    public static Element packHeader(String txCode) throws PackMessageException {
        JZDSFFrontEndConfig feConfig = (JZDSFFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("Message");
        Element head = root.addElement("Head");
        //企业入网编号
        DomUtil.addChild(head, "CorpNo", feConfig.getCorpNo().getVal());
        //接口的版本号
        DomUtil.addChild(head, "Version", feConfig.getVersion().getVal());
        //交易类型编码,20602=代收,20601=代付
        DomUtil.addChild(head, "TranCode", txCode);
        //费项编码
        DomUtil.addChild(head, "FeeNo", feConfig.getFeeNo().getVal());
        return root;
    }

    /**
     * 证件类型:
     * 1   V 其他
     * 2   0 身份证
     * 3   H 户口本
     * 4   O 武装警察身份证
     * 5   P 港澳居民往来通行证
     * 6   M 部队公章证明
     * 7   Q 大陆通行证（台湾）
     * 8   J 港澳台旅行证
     * 9   D 教师证
     * 10  C 军人身份证
     * 11  G 驾驶证
     * 12  A 护照
     * 13  B 学生证
     */
    public static final String CERTIFICATETYPE = "0";//默认：身份证

    /**
     * 获取证件类型
     * @param certType 我们对外提供的接口中所规定的证件类型标识(请参阅 PaymentEntity.EXT_PROPERTY_CertType)
     * @return 通道方所规定的类型标识
     */
    public static String getCertType(String certType) {
        String type = "";
        if ("0".equals(certType)) {
            type = "0";
        } else if ("4".equals(certType)) {
            type = "H";
        } else if ("1".equals(certType)) {
            type = "A";
        }
        return type;
    }

}
