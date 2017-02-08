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
package com.iboxpay.settlement.gateway.jzdsf.service.query;

import java.util.Date;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import cacps.DownloadFileService;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.query.AbstractQueryPayment;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.DomUtil;
import com.iboxpay.settlement.gateway.jzdsf.JZDSFFrontEndConfig;
import com.iboxpay.settlement.gateway.jzdsf.service.CommonParser;
import com.iboxpay.settlement.gateway.jzdsf.service.UploadDownloadClient;
import com.iboxpay.settlement.gateway.jzdsf.service.CommonParser.HeadInfo;

/**
 * 
 * 集中代收付
 * 单笔实时根据企业流水号查询业务状态-For 代收(20602)
 *
 * @author: fengweichao
 *	
 * @2016年3月2日  @下午1:59:22
 */
@Service
public class QueryPayment_20602 extends AbstractQueryPayment {
    private static Logger logger = LoggerFactory.getLogger(QueryPayment_20602.class);
    
    //银行交易码(代收付标识固定填写20601(代付)或20602(代收))
    private String BANK_TRANS_CODE_20602 = "20602";

    
    
    @Override
    public void query(PaymentEntity[] payments) throws BaseTransException {
        JZDSFFrontEndConfig fe = (JZDSFFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        PaymentEntity payment = payments[0];
        int interval = Integer.parseInt(fe.getIntervalTime().getVal());
        long queryTimeMs = payment.getSubmitPayTime().getTime() + interval * 60 * 1000;
        if (new Date().getTime() > queryTimeMs) {
            _query(payments, fe);
        } else {
            PaymentStatus.setStatus(payments, payment.getStatus(), interval + "分钟后才可以发起状态查询", "", "");
        }
    }
    
    private void _query(PaymentEntity[] payments, JZDSFFrontEndConfig fe){
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < payments.length; i++) {
            if (i > 0) ids.append(",");
            ids.append(payments[i].getId());
        }
        try {
            logger.info("准备【查询交易状态】请求报文(paymentId:[{}], batchSeqId:{}): \n{}", ids.toString(), payments[0].getBatchSeqId(), "查询状态无报文");
            
            //发送请求，接收报文
            DownloadFileService downloadFileService = UploadDownloadClient.getDownloadFileService(fe.getDownloadUrl().getVal());
            logger.info("发送请求到【集中代收付】后台. url:" + fe.getDownloadUrl().getVal());
            
            String rsp = downloadFileService.queryRealTimeSingleBySerialNum(fe.getCorpNo().getVal(), DateTimeUtil.format(payments[0].getSubmitPayTime(), "yyyyMMdd"), BANK_TRANS_CODE_20602, fe.getFeeNo().getVal(), payments[0].getBankBatchSeqId());
            logger.info("接收【查询交易状态】返回报文(paymentId:[{}], batchSeqId:{}): \n{}", ids.toString(), payments[0].getBatchSeqId(), rsp);

            // 解析报文
            parse(rsp, payments);
        } catch(Exception e) {
            logger.error("【集中代收付】单笔实时根据企业流水号(" + payments[0].getBankBatchSeqId() + ")查询业务状态出错：", e);
        }
    }

    @Override
    public String pack(PaymentEntity[] payments) throws PackMessageException {
        return null;
    }

    @Override
    public void parse(String respStr, PaymentEntity[] payments) throws ParseMessageException {
        /**
         * 查询支付状态不需要解码解密，返回的是XML明文
         */
        Element root = DomUtil.parseXml(respStr);
        HeadInfo headInfo = CommonParser.parseHead(root);
        if (headInfo.SUCCESS) {
            PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "交易成功", headInfo.CODE, headInfo.MESSAGE);
        } else {
            PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "交易失败", headInfo.CODE, headInfo.MESSAGE);
        }
    }
    
    @Override
    public String getBankTransCode() {
        return BANK_TRANS_CODE_20602;
    }

    @Override
    public String getBankTransDesc() {
        return "单笔实时根据企业流水号查询业务状态";
    }

}

	