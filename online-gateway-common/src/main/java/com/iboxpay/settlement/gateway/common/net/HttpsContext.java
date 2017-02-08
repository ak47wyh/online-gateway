package com.iboxpay.settlement.gateway.common.net;

import java.io.File;

/**
 * 
 * The class ISslStore.
 *
 * Description: Https证书相关信息类
 *
 * @author: jianbo_chen
 * @since: 2015年5月11日	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public interface HttpsContext {

    //双向验证时需要私匙证书
    /**私匙证书格式 */
    String keyStoreType();

    /**私钥保存文件*/
    File keyStoreFile();

    /**私匙保存库密码*/
    String keyStorePassword();

    /**私匙密码*/
    String keyPassword();

    /**是否信任公钥（包括证书与域名检查）*/
    boolean trustCertification();
}
