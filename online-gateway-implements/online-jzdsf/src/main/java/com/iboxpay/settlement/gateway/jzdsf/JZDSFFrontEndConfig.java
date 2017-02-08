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
package com.iboxpay.settlement.gateway.jzdsf;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;

/**
 * 集中代收付  前置机
 *
 * @author: fengweichao
 *	
 * @2016年2月22日  @下午4:48:51
 */
public class JZDSFFrontEndConfig extends FrontEndConfig {
    private static final long serialVersionUID = 1L;

    //UploadFileService服务url
    private Property uploadUrl;
    //DownloadFileService服务url
    private Property downloadUrl;
    //入网分配的用户名
    private Property userCode;
    //入网时分配的企业编号
    private Property corpNo;
    //费项编码
    private Property feeNo;
    //接口的版本号
    private Property version;
    
    //SNCA证书文件
    private Property pfxFile;
    //SNCA证书密码
    private Property pfxPass;
    
    //支付交易发生后，查询交易结果状态的时间间隔
    private Property IntervalTime; 
    
    public JZDSFFrontEndConfig(){
        setDefVal(protocal, "http");
        setDefVal(charset, "UTF-8");
        setReadOnly(protocal);
        setReadOnly(charset);
        
        uploadUrl = new Property("uploadUrl", "UploadFileService服务接口url");
        downloadUrl = new Property("downloadUrl", "DownloadFileService服务接口url");
        userCode = new Property("userCode", "入网分配的用户名");
        corpNo = new Property("corpNo", "入网时分配的企业编号");
        feeNo = new Property("feeNo", "费项编码");
        version = new Property("version", "V1.00", "接口的版本号");
        
        pfxFile = new Property("pfxFile", Property.Type.file, "SNCA证书文件(.pfx)");
        pfxPass = new Property("pfxPass", "11111111", "SNCA证书密码");
        
        IntervalTime = new Property("IntervalBigBatch", "10", "发起交易后，多长时间（分钟）可以查询交易状态，默认10分钟");
    }
    
    
    public Property getUploadUrl() {
        return uploadUrl;
    }
    public Property getDownloadUrl() {
        return downloadUrl;
    }
    public Property getUserCode() {
        return userCode;
    }
    public Property getCorpNo() {
        return corpNo;
    }
    public Property getFeeNo() {
        return feeNo;
    }
    public Property getVersion() {
        return version;
    }
    public Property getPfxFile() {
        return pfxFile;
    }
    public Property getPfxPass() {
        return pfxPass;
    }
    public Property getIntervalTime() {
        return IntervalTime;
    }
    
}

	