/*
 * Copyright (C) 2011-2013 ShenZhen iBOXPAY Information Technology Co.,Ltd.
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
 * $Id: SignatureUtil.java 4696 2013-07-18 01:22:21Z deli $
 * 
 * Create on 2012-1-14
 * 
 * Description: 
 *
 */

package com.iboxpay.settlement.gateway.alipay.servie.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.common.bytecode.EncodeUtils;
import com.iboxpay.common.cst.CommonCst;
import com.iboxpay.common.security.CryptUtils;
import com.iboxpay.common.security.RSACoder;

/**
 * @author <a href="falchion123@gmail.com">liuzhonghui</a>
 */
public class SignatureUtils {

    private final static Logger LOG = LoggerFactory.getLogger(SignatureUtils.class);

    /**
     * MD5
     */
    public static final String SIGNATURE_MD5 = "MD5";

    /**
     * RSA
     */
    public static final String SIGNATURE_RSA = "RSA";

    /**
     * DSA
     */
    public static final String SIGNATURE_DSA = "DSA";

    /**
     * MD5
     */
    public static final String MD5 = "1";

    /**
     * RSA
     */
    public static final String RSA = "2";

    /**
     * DSA
     */
    public static final String DSA = "3";

    /**
     * 签名.
     * 
     * @param source
     * @param signType
     * @param inputCharset
     * @param key
     * @return
     */
    public static String sign(String source, String signType, String key) {
        String data = StringUtils.EMPTY;
        LOG.info("source is {},key is {}", source, key);
        try {

            String charset = CommonCst.CHARSET_UTF_8_NAME;
            if (signType.equalsIgnoreCase(SIGNATURE_MD5)) {
                String src = source + key;
                data = CryptUtils.encryptToMD5(src.getBytes(charset));

            } else if (signType.equalsIgnoreCase(SIGNATURE_RSA)) {
                data = EncodeUtils.bytesToHexString(RSACoder.sign(source.getBytes(charset), EncodeUtils.hexStringToByte(key)));
            }
            return data;
        } catch (Exception e) {
            LOG.error("Error when signature, errmsg: ", e);
        }
        return data;
    }

    /**
     * @param source
     * @param signMsg
     * @param signType
     * @param key
     * @param inputCharset
     * @return
     */
    public static boolean verify(String source, String signMsg, String signType, String key) {
        boolean ret = false;

        String data = null;
        String charset = CommonCst.CHARSET_UTF_8_NAME;
        try {
            if (signType.equalsIgnoreCase(SIGNATURE_MD5)) {
                String src = source + key;
                data = CryptUtils.encryptToMD5(src.getBytes(charset));

                if (signMsg.equalsIgnoreCase(data)) {
                    ret = true;
                }
            } else if (signType.equalsIgnoreCase(SIGNATURE_RSA)) {
                ret = RSACoder.verifyByPublicKey(source.getBytes(charset), EncodeUtils.hexStringToByte(key), EncodeUtils.hexStringToByte(signMsg));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error when verify msg, errmsg: " + e.getMessage(), e);
        }

        return ret;
    }

    /**
     * @param params
     * @return
     */
    public static String getContent(Map<String, Object> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        boolean first = true;
        Object value = null;
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            value = params.get(key);
            if (null == value || "".equals(value) || "null".equals(value)) {
                continue;
            }
            if (first) {
                sb.append(key).append("=").append(value);
                first = false;
            } else {
                sb.append("&").append(key).append("=").append(value);
            }
        }
        return sb.toString();
    }
}
