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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.util.Base64;
import com.iboxpay.settlement.gateway.jzdsf.JZDSFFrontEndConfig;
import com.snca.pfx.env.dec.SncaEnvDec;
import com.snca.swxa.env.enc.SncaEnvEnc;

/**
 * 
 * 集中代收付，加密解密工具类
 *
 * @author: fengweichao
 *	
 * @2016年3月3日  @下午4:41:40
 */
public class SncaEnvEncDecUtil {
    private static Logger logger = LoggerFactory.getLogger(SncaEnvEncDecUtil.class);
    
    public static String encodeXml(String xml){
        JZDSFFrontEndConfig fe = (JZDSFFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        try {
            xml = new String(new SncaEnvEnc().SncaEnvEncStr(xml));
            xml = new String(Base64.encodeString(xml));
        } catch (Exception e) {
            logger.error("集中代收付【加密编码】发送的XML数据出错！", e);
        }
        return xml;
    }
    
    public static String decodeRespToXML(String respStr){
        JZDSFFrontEndConfig fe = (JZDSFFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        try {
            /**
             * 首先使用Base64 位编码对得到的结果报文进行解码（字符集编码：UTF-8）;
             * 使用企业入网时发给企业的CFCA 私钥对Base64 解码后的字节流进行解密，
             * 得到XML 报文结果（字符集编码：UTF-8）
             */
            respStr = new String(Base64.decodeToString(respStr));
//            respStr = new String(SncaEnvDec.SncaEnvDecByte(fe.getPfxFile().getFileVal().getAbsolutePath(), fe.getPfxPass().getVal(), respStr));
            
            System.out.println("=========\n" + respStr);
            logger.info("解码解密】后的明文结果：" + respStr);
            
        } catch (Exception e) {
            logger.error("集中代收付【解码解密】返回的数据出错！", e);
        }
        return respStr;
    }
    
    private static void test2(){
        
        String resp = "s4Cdi4TXTD2xdbDa1JULE77Maddv5xBv8v7pS7U2nrm+RH8zwhCWop/8hVUQkpAMoQiZsWP0Usn9lfra+4M3iBosXYIvW5BSQBHFuhje5inxcdstJOifCyTqNw9Gef14|bGcyt5LrBUBieOnTr1OxtCic9i2xaKSVjGLF4rqi83S4NuIBMG2g9q3/HZrByAB0XtIOFXzaEpteVLQbCMPEzUxHYrQeYWkwRLNuoYwvGYmE8lZrR/VdjCg/qQgUqcSLjdMvEQiWxSNhlrLSOtHt1EdXY+b3oXQJ89srkkQL7ZM=";
        
        try {
//            resp = new String(Base64.decodeToString(resp));
            
            String path = SncaEnvEncDecUtil.class.getClassLoader().getResource("").getPath();
            String file = path + "1.pfx";
            resp = SncaEnvDec.SncaEnvDecStr(file, "11111111", resp);
            
//            byte[] bytestr = SncaEnvDec.SncaEnvDecByte(file, "11111111", resp);
//            resp = new String(bytestr);
            
            System.out.println(resp);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("================");
    }
    
    private static void test(){
//        String path = SncaEnvDecUtil.class.getResource("/").toString();
        String path = SncaEnvEncDecUtil.class.getClassLoader().getResource("/").getPath();
        System.out.println(path);
    }
    
    public static void main(String[] args) {
        test2();
    }
    
}

	