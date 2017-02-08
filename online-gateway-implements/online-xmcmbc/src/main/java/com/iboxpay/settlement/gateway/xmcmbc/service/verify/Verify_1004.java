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
package com.iboxpay.settlement.gateway.xmcmbc.service.verify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.VerifyStatus;
import com.iboxpay.settlement.gateway.common.trans.verify.IAccountVerify;
import com.iboxpay.settlement.gateway.common.util.DomUtil;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.xmcmbc.Constants;
import com.iboxpay.settlement.gateway.xmcmbc.SocketDiffHelper;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcFrontEndConfig;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcHelper;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonPacker;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser.HeadInfo;
import com.iboxpay.settlement.gateway.xmcmbc.service.RSAHelper;

@Service
public class Verify_1004 extends ConnectionAdapter implements IAccountVerify {
	private static Logger logger = LoggerFactory.getLogger(Verify_1004.class);
	
	public static final String BANK_TRANS_CODE = "1004";
	@Override
	public TransCode getTransCode() {
		return TransCode.VERIFY;
	}

	@Override
	public String getBankTransCode() {
		return BANK_TRANS_CODE;
	}

	@Override
	public String getBankTransDesc() {
		return "厦门民生实名认证";
	}
	
	public byte[] pack(AccountVerifyEntity account, String serialNo) throws PackMessageException {
		byte[] proxyBytes = null;
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig)context.getFrontEndConfig();
		String charsetName = config.getCharset().getVal();
		File privateKeyFile = config.getPrivateKeyFile().getFileVal();
		File publicKeyFile = config.getPublicKeyFile().getFileVal();
        String poxy = config.getPoxy().getVal();
        String accNo = account.getCustomerAccNo();
		String accName = account.getCustomerAccName();
		/** 证件类型*/
		String certType = account.getCertType();
		certType = XmcmbcHelper.initCertType(certType);
		/** 证件号码*/
		String certNo = account.getCertNo();
		// 手机号
		String mobileNo = account.getMobileNo();
		
		try {
			byte[] bytes = null;
			Element root = CommonPacker.diffPackHeader();
			DomUtil.addChild(root, "SerialNo", serialNo);//渠道流水号,唯一
			//DomUtil.addChild(root, "MerId", "");//商户号
			//DomUtil.addChild(root, "MerName", "");//商户名（机构名称）
			/**
			 * 卡折标志 0-	借记卡（默认）1-存折2-贷记卡（信用卡）3-公司账号
			 */
			if (account.getCustomerCardType() == 1) {
				DomUtil.addChild(root, "CardType", "1");
			} else if (account.getCustomerCardType() == 2) {
				DomUtil.addChild(root, "CardType", "2");
			} else if (account.getCustomerCardType() == 3) {
				DomUtil.addChild(root, "CardType", "3");
			} else {
				DomUtil.addChild(root, "CardType", "0");
			}

			DomUtil.addChild(root, "AccNo", accNo);//账户号
			DomUtil.addChild(root, "AccName", accName);//账户名
			DomUtil.addChild(root, "CertType", certType);//证件类型
			DomUtil.addChild(root, "CertNo", certNo);//证件号码
			DomUtil.addChild(root, "Phone", mobileNo);//付款人手机号码
			DomUtil.addChild(root, "Resv", "");//备用域

			String xml = DomUtil.documentToString(root.getDocument(), charsetName);
			logger.info("发送【实名认证】报文: \n" + xml);
			byte[] bodyBytes = xml.getBytes(charsetName);
			RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
			byte[] signData = RSAHelper.signRSA(bodyBytes, false, charsetName);
			byte[] encrtptData = RSAHelper.encryptRSA(bodyBytes, false, charsetName);

			byte[] packBytes = null;
			packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(config.getCompanyId().getVal(), 8, " ").getBytes(charsetName));//8位合作方编号，位数不足左补空格
			packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(BANK_TRANS_CODE, 8, " ").getBytes(charsetName));//8位交易码，位数不足左补空格
			packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad("256", 4, "0").getBytes(charsetName));//4位签名域长度，右对齐左补零
			packBytes = ArrayUtils.addAll(packBytes, signData);//签名域值
			packBytes = ArrayUtils.addAll(packBytes, encrtptData);//XML报文数据主体密文

			bytes = ArrayUtils.addAll(bytes, StringUtils.leftPad(String.valueOf(packBytes.length), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
			bytes = ArrayUtils.addAll(bytes, packBytes);
			
			// 与代理定义的协议
			if("true".equals(poxy)) {
				int len = bytes.length + 4 + serialNo.getBytes(charsetName).length;
				proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(len), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
				proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(serialNo.getBytes(charsetName).length), 4, "0").getBytes(charsetName));//4位订单号长度，右对齐左补零
				proxyBytes = ArrayUtils.addAll(proxyBytes, serialNo.getBytes(charsetName));
				proxyBytes = ArrayUtils.addAll(proxyBytes, bytes);
			} else {
				proxyBytes = bytes;
			}
		} catch (Exception e) {
			throw new PackMessageException("实名认证发生异常:" + e);
		}
        
		return proxyBytes;
	}

	@Override
	public void verfiy(AccountVerifyEntity account) throws BaseTransException, IOException {
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig)context.getFrontEndConfig();
		String charsetName = config.getCharset().getVal();
		File privateKeyFile = config.getPrivateKeyFile().getFileVal();
		File publicKeyFile = config.getPublicKeyFile().getFileVal();
		String ip = config.getIp().getVal();
        int diffPort = config.getDiffPort().getIntVal();
        int timeout = config.getTimeout().getIntVal() * 1000;
        String poxy = config.getPoxy().getVal();
        String serialNo = Sequence.genSequence();
        
        byte[] rsqt = pack(account, serialNo);
        String rspStr = null;
        logger.info("准备【验证】报文(verifyId:[{}], batchSeqId:{})", account.getId().toString(), account.getBatchSeqId());
		if("false".equals(poxy)) {
    		try {
    			SocketDiffHelper.config = config;
    			SocketDiffHelper.put(serialNo, rsqt);
				SocketDiffHelper.countDownMap.get(serialNo).await(timeout, TimeUnit.SECONDS);
				rspStr = SocketDiffHelper.rspMap.get(serialNo);
			} catch (Exception e) {
				logger.error("", e);
			} finally {
				SocketDiffHelper.clear(serialNo);
			}
		} else {
    		openConnection(ip, diffPort);
            OutputStream os = getOutputStream();
            try {
                //发送请求
                send(os, rsqt);
                handleAfterWrite(os);
                // 获取输入流
                InputStream is = getInputStream();
                // 接收报文
                byte[] bytes = CommonParser.readDiffByte(is);
                
                handleAfterRead(is);
                
                if(bytes != null) {
    				// 对密文解密
    				RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
    				byte[] decryptedBytes = RSAHelper.decryptRSA(bytes, false, charsetName);
    				rspStr = new String(decryptedBytes, charsetName);
    			}
            } catch (Throwable e) {
                logger.error("", e);
                VerifyStatus.processExceptionWhenPay(e, account);
            }
		}
		
		logger.info("接收【验证】返回报文(verifyId:[{}], batchSeqId:{}): \n{}", account.getId().toString(), account.getBatchSeqId(), rspStr);
        // 解析报文
        parse(rspStr, account, serialNo);
	}
	
	public void parse(String resp, AccountVerifyEntity verify, String serialNo)
			throws ParseMessageException {
		//返回的应答报文:decrypted: <?xml version="1.0" encoding="UTF-8"?><TRAN_RESP><RESP_TYPE>S</RESP_TYPE><RESP_CODE>000000</RESP_CODE><RESP_MSG>交易成功</RESP_MSG><MCHNT_CD>201511230000042</MCHNT_CD><TRAN_DATE>20160229</TRAN_DATE><TRAN_TIME>153623</TRAN_TIME><TRAN_ID>4jz00006pq</TRAN_ID><BANK_TRAN_ID>2016022909786628</BANK_TRAN_ID><BANK_TRAN_DATE>20160229</BANK_TRAN_DATE><BANK_TRAN_TIME>154154</BANK_TRAN_TIME><CHARGE_FEE>0</CHARGE_FEE><RESV></RESV></TRAN_RESP>
		try {
			Element root = DomUtil.parseXml(resp);
			HeadInfo headInfo = CommonParser.diffParseHead(root);
			String qeqSerialNo = DomUtil.getTextTrim(root, "ReqSerialNo");//原交易流水
			String validateStatus = DomUtil.getTextTrim(root, "ValidateStatus");//认证状态 00-认证成功 99-认证失败
			if (serialNo.equalsIgnoreCase(qeqSerialNo)) {//防止返回的报文不是对应的支付报文
				verify.setSeqId(serialNo);
				verify.setErrorCode(headInfo.code);
				if (Constants.RESPONSE_CODE_SUCCESS.equals(headInfo.code) && Constants.RESPONSE_VALIDATE_SUCCESS.equals(validateStatus)) {//直接成功
					VerifyStatus.setStatus(verify, VerifyStatus.STATUS_SUCCESS, "验证成功", validateStatus, headInfo.message);
				} else if (XmcmbcHelper.isFail(headInfo.code)) {//直接失败
					VerifyStatus.setStatus(verify, VerifyStatus.STATUS_FAIL, "验证失败", validateStatus, headInfo.message);
				} else {//未确定的，查询确定
					VerifyStatus.setStatus(verify, VerifyStatus.STATUS_UNKNOWN, "未确定", "", "");
				}
			} else {
				VerifyStatus.setStatus(verify, VerifyStatus.STATUS_UNKNOWN, "未确定", "", "");
			}
		} catch (Exception e) {
			VerifyStatus.setStatus(verify, VerifyStatus.STATUS_UNKNOWN, "响应报文体解密异常,查询确认", "", "");
			throw new ParseMessageException("解密发生异常:"+e);
		}
	}
}

	