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

import java.io.File;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.jz.JzFrontEndConfig;
import com.ju.utils.EncryptUtils;
/**
 * 加密/解密
 * @author caolipeng
 * @date 2015年7月17日 下午5:38:51
 * @Version 1.0
 */
public class SignatureFacade {

    private static long privateKeyFileModified;
    private static String privateKeyUrl;//密钥URL

    private static void readCertKey() throws BaseTransException {
        JzFrontEndConfig feConfig = (JzFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        File privateKeyFile = feConfig.getPrivateKeyFile().getFileVal();
        if (privateKeyFileModified == 0L || privateKeyFileModified < privateKeyFile.lastModified()) {
            synchronized (SignatureFacade.class) {
            	if(privateKeyFile !=null && privateKeyFile.exists()){//目录存在
            		privateKeyUrl = privateKeyFile.getPath();
            	} else {
            		throw new BaseTransException("读取证书失败:"+privateKeyFile.getParent());
            	}
                privateKeyFileModified = privateKeyFile.lastModified();
            }
        }
    }
    /**
     * 加密
     * @param str 待加密字符串
     * @throws PackMessageException 封装报文异常
     */
    public static String encrypt(String str) throws PackMessageException{
    	String encryptStr = "";
    	try {
			readCertKey();//加载证书
			encryptStr = EncryptUtils.encrypt(str, privateKeyUrl);
		} catch (BaseTransException e) {
			throw new PackMessageException("读取密钥证书出错", e);
		} catch (Exception e) {
			throw new PackMessageException("矩阵加密发生异常", e);
		}
    	return encryptStr;
    }
    /**
     * 签名方法
     * @param str  待签名字符串:为商户号+交易码+报文
     * @return signature
     * @throws PackMessageException
     */
    public static String juSignature(String str) throws PackMessageException{
    	String signature = "";
    	try {
    		readCertKey();//加载证书
			signature = EncryptUtils.juSignature(str, privateKeyUrl);
		}  catch (BaseTransException e) {
			throw new PackMessageException("读取密钥证书出错", e);
		}  catch (Exception e) {
			throw new PackMessageException("矩阵加密发生异常", e);
		}
    	return signature;
    }
    /**
     * 验签
     * @param str 待验证签名字符串
     * @return 验签成功,返回true.否则返回false
     * @throws ParseMessageException
     */
    public static boolean juValidateSignature(String str) throws ParseMessageException{
    	boolean flag =false;
    	try {
    		readCertKey();//加载证书
			flag = EncryptUtils.juValidateSignature(str, privateKeyUrl);
		} catch (BaseTransException e) {
			throw new ParseMessageException("读取密钥证书出错", e);
		} catch (Exception e) {
			throw new ParseMessageException("验签失败", e);
		}
    	return flag;
    }
    
    public static void main(String[] args) {
    	String privateKeyUrl = "E:\\结算平台\\银企网关\\银企直联\\矩阵代收付\\000000000000012.key";
    	File file = new File(privateKeyUrl);
    	String filePath = file.getPath();
    	System.out.println(filePath);
	}
}
