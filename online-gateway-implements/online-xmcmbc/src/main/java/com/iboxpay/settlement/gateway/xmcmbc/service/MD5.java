package com.iboxpay.settlement.gateway.xmcmbc.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * The class MD5.
 *
 * Description: 
 *
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public class MD5 {

    private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String encode(byte[] binaryData) {
        java.security.MessageDigest md = null;
        try {
            md = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(binaryData);
        byte tmp[] = md.digest();
        char str[] = new char[16 * 2];
        int k = 0;
        for (int i = 0; i < 16; i++) {
            byte byte0 = tmp[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);// 换后的结果转换为字符串
    }

    public static String encode(String data) {
        try {
            return encode(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
