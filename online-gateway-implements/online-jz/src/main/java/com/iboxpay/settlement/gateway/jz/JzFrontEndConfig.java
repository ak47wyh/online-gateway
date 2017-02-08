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
package com.iboxpay.settlement.gateway.jz;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.config.Property.Type;
/**
 * 矩阵渠道前置机配置
 * @author caolipeng
 * @date 2015年8月3日 下午2:00:23
 * @Version 1.0
 */
public class JzFrontEndConfig extends FrontEndConfig {

    private static final long serialVersionUID = 1L;
    /**服务uri*/
    private Property uri;
    /**密钥证书*/
	private Property privateKeyFile;
    
    public JzFrontEndConfig() {
        //http协议用于余额查询时候使用
        setDefVal(protocal, "http");
        setReadOnly(protocal);
        setDefVal(charset, "UTF-8");
        setReadOnly(charset);
        uri = new Property("uri", "http请求的URI");
        privateKeyFile = new Property("privateKeyFile", Type.file, "密钥证书文件(.key)");
    }

	public Property getUri() {
		return uri;
	}

	public Property getPrivateKeyFile() {
		return privateKeyFile;
	}

}
