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

import java.net.MalformedURLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caucho.hessian.client.HessianProxyFactory;
import cacps.DownloadFileService;
import cacps.UploadFileService;

/**
 * 集中代收付 - 发送请求业务对象获取工具类
 *
 * @author: fengweichao
 *	
 * @2016年3月1日  @下午5:26:22
 */
public class UploadDownloadClient {
    private static Logger logger = LoggerFactory.getLogger(UploadDownloadClient.class);
    private static UploadFileService unique;
    private static DownloadFileService unique2;
    
    private UploadDownloadClient(){
        
    }
    
    public static UploadFileService getUploadFileService(String url) {
//        String url = "http://192.168.1.1:7001/cacps/uploadfileServer";
        if(null == unique){
            unique = UploadFileServiceCreator.create(url);
        }
        return unique;
    }
    
    public static DownloadFileService getDownloadFileService(String url) {
//        String url = "http://192.168.1.1:7001/cacps/downloadfileServer";
        if(null == unique2){
            unique2 = DownloadFileServiceCreator.create(url);
        }
        return unique2;
    }
    
    /**
     * 
     * 采用单例模式创建UploadFileService对象，以避免大量创建对象，内存开销过大
     *
     * @author: fengweichao
     *	
     * @2016年3月1日  @下午5:47:46
     */
    private static class UploadFileServiceCreator {

        public static UploadFileService create(String url) {
            HessianProxyFactory factory = new HessianProxyFactory();
            UploadFileService uploadFileService = null;
            try {
                uploadFileService = (UploadFileService) factory.create(UploadFileService.class, url);
            } catch (MalformedURLException e) {
                System.out.println("occur exception: " + e);
                logger.info("#######创建UploadFileService对象出错#######: " + e);
            }
            return uploadFileService;
        }
    }
    
    /**
     * 
     * 采用单例模式创建DownloadFileService对象，以避免大量创建对象，内存开销过大
     *
     * @author: fengweichao
     *	
     * @2016年3月1日  @下午5:47:46
     */
    private static class DownloadFileServiceCreator {
        
        public static DownloadFileService create(String url) {
            HessianProxyFactory factory = new HessianProxyFactory();
            DownloadFileService downloadFileService = null;
            try {
                downloadFileService = (DownloadFileService) factory.create(DownloadFileService.class, url);
            } catch (MalformedURLException e) {
                System.out.println("occur exception: " + e);
                logger.info("#######创建DownloadFileService对象出错#######: " + e);
            }
            return downloadFileService;
        }
    }
}

	