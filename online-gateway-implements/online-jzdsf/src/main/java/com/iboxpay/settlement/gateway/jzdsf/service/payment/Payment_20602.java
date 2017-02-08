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
package com.iboxpay.settlement.gateway.jzdsf.service.payment;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import cacps.UploadFileService;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.payment.AbstractPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.util.DomUtil;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.jzdsf.JZDSFFrontEndConfig;
import com.iboxpay.settlement.gateway.jzdsf.service.CommonPacker;
import com.iboxpay.settlement.gateway.jzdsf.service.CommonParser;
import com.iboxpay.settlement.gateway.jzdsf.service.CommonParser.HeadInfo;
import com.iboxpay.settlement.gateway.jzdsf.service.SncaEnvEncDecUtil;
import com.iboxpay.settlement.gateway.jzdsf.service.UploadDownloadClient;
import com.iboxpay.settlement.gateway.jzdsf.service.query.QueryPayment_20602;

/**
 * 集中代收付
 * 单笔实时业务接口-代收(20602)
 *
 * @author: fengweichao
 *	
 * @2016年2月23日  @上午10:11:08
 */
@Service
public class Payment_20602 extends AbstractPayment {
    private static Logger logger = LoggerFactory.getLogger(Payment_20602.class);

    //银行交易码(即：该支付接口交易码)
    private String BANK_TRANS_CODE_20602 = "20602";
    // 01= 信用卡;02= 借记卡;
    private String CARDTYPE = "02";//默认只支持借记卡
    
    @Override
    public void pay(PaymentEntity[] payments) throws BaseTransException {
        JZDSFFrontEndConfig fe = (JZDSFFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < payments.length; i++) {
            if (i > 0) ids.append(",");
            ids.append(payments[i].getId());
        }
        try {
            String xml = pack(payments);
            logger.info("准备【支付】报文(paymentId:[" + ids + "], batchSeqId:" + payments[0].getBatchSeqId() + "): \n" + xml);
            try {
                //发送请求，接收报文
                UploadFileService uploadFileService = UploadDownloadClient.getUploadFileService(fe.getUploadUrl().getVal());
                logger.info("发送请求到【集中代收付】后台. url:" + fe.getUploadUrl().getVal());
                
                //加密编码XML报文
                xml = SncaEnvEncDecUtil.encodeXml(xml);
                String rsp = uploadFileService.realTimeSingle(fe.getUserCode().getVal(), fe.getCorpNo().getVal(), BANK_TRANS_CODE_20602, xml);
                logger.info("接收【支付】返回报文(paymentId:[" + ids + "], batchSeqId:" + payments[0].getBatchSeqId() + "): \n" + rsp);
                // 解析报文
                parse(rsp, payments);
            } catch (Throwable e) {
                logger.error("", e);
                PaymentStatus.processExceptionWhenPay(e, payments);
            }
        } catch (Throwable e) {
            logger.error("", e);
            PaymentStatus.processExceptionBeforePay(e, payments);
        }
    }
    
    /**
     * 企业流水号（8 位唯一码）
     */
    @Override
    public void genBankBatchSeqId(PaymentEntity[] payments) {
        String bankBatchSeqId = Sequence.genSequence(8);
        for (PaymentEntity payment : payments){
            payment.setBankBatchSeqId(bankBatchSeqId);
        }
    }

    @Override
    public String pack(PaymentEntity[] payments) throws PackMessageException {
        JZDSFFrontEndConfig fe = (JZDSFFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        AccountEntity account = TransContext.getContext().getMainAccount();
        PaymentEntity payEntity = payments[0];
        Element root = CommonPacker.packHeader(BANK_TRANS_CODE_20602);
        Element in = root.addElement("In");
        //企业流水号（8 位唯一码）
        DomUtil.addChild(in, "SerialNum", payEntity.getBankBatchSeqId());
        //付款人：协议号(代扣可不填)
        DomUtil.addChild(in, "DbtrProtocol", "");
        //付款人：姓名
        DomUtil.addChild(in, "DbtrActName", payEntity.getCustomerAccName());
        //付款人：账号
        DomUtil.addChild(in, "DbtrActId", payEntity.getCustomerAccNo());
        //付款人： 账户类型： 01= 信用卡;02= 借记卡;
        DomUtil.addChild(in, "DbtrCardType", CARDTYPE);
        //付款人：证件类型
        DomUtil.addChild(in, "DbtrCertType", CommonPacker.getCertType(payEntity.getExtProperty(PaymentEntity.EXT_PROPERTY_CertType).toString()));
        //付款人：证件号
        DomUtil.addChild(in, "DbtrCertNo", payEntity.getExtProperty(PaymentEntity.EXT_PROPERTY_CertNo).toString());
        //付款人：信用卡CNV2
        DomUtil.addChild(in, "DbtrCardCVN2", "");
        //付款人：办卡注册的手机号
        DomUtil.addChild(in, "DbtrPhone", payEntity.getExtProperty(PaymentEntity.EXT_PROPERTY_MOBILENO).toString());
        //付款人：信用卡有效期
        DomUtil.addChild(in, "DbtrCardExpired", "");
        //付款行：银行类别  (联行号前三位)
        DomUtil.addChild(in, "DbtrBankType", payEntity.getCustomerCnaps().substring(0, 3));
        //付款行：联行号
        DomUtil.addChild(in, "DbtrBankId", payEntity.getCustomerCnaps());
        //付款行：省名
        DomUtil.addChild(in, "DbtrBankProvince", "");
        //付款行：城市
        DomUtil.addChild(in, "DbtrBankCity", "");
        //付款行：开户行名称
        DomUtil.addChild(in, "DbtrBankName", payEntity.getCustomerBankBranchName());
        //收款人：名称
        DomUtil.addChild(in, "CdtrActName", account.getAccName());
        //收款人：账号
        DomUtil.addChild(in, "CdtrActId", account.getAccNo());
        //交易金额，以“元”为单位，保留2 位小数
        DomUtil.addChild(in, "PayAmt", payEntity.getAmount().toString());
        //订货人姓名：[可选]
        DomUtil.addChild(in, "CustName", "");
        //订货人邮箱：[可选]
        DomUtil.addChild(in, "CustEmail", "");
        //商品信息：[可选]
        DomUtil.addChild(in, "ProductInfo", "");
        //备注信息：[可选]
        DomUtil.addChild(in, "Remark", "");
        //用途：“保费”
        DomUtil.addChild(in, "Use", "");
        return DomUtil.documentToString(root.getDocument(), fe.getCharset().getVal());
    }

    @Override
    public void parse(String respStr, PaymentEntity[] payments) throws ParseMessageException {
        /**
         * 
         * 首先使用Base64 位编码对得到的结果报文进行解码（字符集编码：UTF-8）;
         * 使用企业入网时发给企业的CFCA 私钥对Base64 解码后的字节流进行解密，
         * 得到XML 报文结果（字符集编码：UTF-8）
         */
        respStr = SncaEnvEncDecUtil.decodeRespToXML(respStr);
        Element root = DomUtil.parseXml(respStr);
        HeadInfo headInfo = CommonParser.parseHead(root);
        if (headInfo.SUCCESS) {
            PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "交易成功", headInfo.CODE, headInfo.MESSAGE);
        } else {
            PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "交易失败", headInfo.CODE, headInfo.MESSAGE);
        }
    }
    
    /**
     * 单笔，同行、跨行、对公、对私
     */
    @Override
    public PaymentNavigation navigate() {
        return PaymentNavigation.create()
                .setSameBank(true)
                .setDiffBank(true)
                .setToCompany(true)
                .setToPrivate(true)
                .setBatchSize(1);
    }

    @Override
    public String check(PaymentEntity[] payments) {
        return null;
    }

    @Override
    public Class<? extends IQueryPayment> getQueryClass() {
        return QueryPayment_20602.class;
    }

    @Override
    public String getBankTransCode() {
        return BANK_TRANS_CODE_20602;
    }

    @Override
    public String getBankTransDesc() {
        return "集中代收付-单笔实时业务接口-代收";
    }

}

	