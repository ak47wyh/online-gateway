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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.common.json.MapperUtils;

/**
 * 
 * SHA1加密算法
 *
 * @author: yinchao
 * @since: 2016年1月9日	
 *
 */
public class EncrytUtils {

    private static final Logger LOG = LoggerFactory.getLogger(EncrytUtils.class);

    private EncrytUtils() {

    }

    //SHA1加密
    public static String SHA1Encrypt(String decript) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(decript.getBytes("UTF-8"));
            byte messageDigest[] = digest.digest();
            
            return byteToHex(messageDigest);
//            StringBuffer hexString = new StringBuffer();
//            // 字节数组转换为 十六进制 数
//            for (int i = 0; i < messageDigest.length; i++) {
//                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
//                if (shaHex.length() < 2) {
//                    hexString.append(0);
//                }
//                hexString.append(shaHex);
//            }
//            return hexString.toString();

        } catch (Exception e) {
            LOG.error("SHA1 Encrypt error" + e);
        }
        return null;
    }

    
    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
    
    
    /**
     * 按字母排列顺序生成加密字符串
     * @param map
     */
    public static String buildSignParms(Map<String, Object> map) {
        //把map用treemap包装默认按参数首字母顺序进行排序
        TreeMap<String, Object> treeMap = new TreeMap<String, Object>(map);
        Set<Entry<String, Object>> keSet = treeMap.entrySet();
        StringBuilder sb = new StringBuilder();
        for (Iterator<Entry<String, Object>> itr = keSet.iterator(); itr.hasNext();) {
            Map.Entry<String, Object> obj = (Map.Entry<String, Object>) itr.next();
            Object keyName = obj.getKey();
            Object keyValue = obj.getValue();
            //处理request请求中map参数value为数组的情况
            if (keyValue instanceof String[]) {
                keyValue = ((String[]) keyValue)[0];
            }
            if (StringUtils.isBlank(sb.toString())) {
                sb.append(keyName + "=" + keyValue);
            } else {
                sb.append("&" + keyName + "=" + keyValue);
            }
        }
        return sb.toString();
    }
    public static void main(String[] args) {
        Map<String, Object> ticketMap = new HashMap<String, Object>();
        ticketMap.put("noncestr", "111");
        ticketMap.put("jsapi_ticket", "222");
        ticketMap.put("timestamp", "333");
        ticketMap.put("url", "4444");
        LOG.info("-------the sign paramMap:{}", MapperUtils.toJson(ticketMap));
        //按字母排列顺序生成加密字符串
        String signStr = buildSignParms(ticketMap);
        
        System.out.println(SHA1Encrypt("jsapi_ticket=kgt8ON7yVITDhtdwci0qeQ7cGG9sfl8LakelCLOKga_EdUG5BLZB2xbOV19Fi01_h97LhIhK16-nlPodp67SPg&noncestr=f57add3c-c4c1-4299-8806-22a79b762ae4&timestamp=1460179383&url=http%3A%2F%2Fapi.test.iboxpay.com%2Fh5%2Flifec%2Fmycode.html%3Fcode%3D0211b34c0616ce1284b429a7e451aceQ%26state%3DSTATE%2523wechat_redirect"));
    }
}
