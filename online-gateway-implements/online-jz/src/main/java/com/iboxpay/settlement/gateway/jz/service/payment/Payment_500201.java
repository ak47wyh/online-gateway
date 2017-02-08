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
package com.iboxpay.settlement.gateway.jz.service.payment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.service.AccountVerifyService;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.VerifyStatus;
import com.iboxpay.settlement.gateway.common.trans.payment.AbstractPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.jz.Constants;
import com.iboxpay.settlement.gateway.jz.JzAccountEntityExt;
import com.iboxpay.settlement.gateway.jz.JzFrontEndConfig;
import com.iboxpay.settlement.gateway.jz.service.CommonPacker;
import com.iboxpay.settlement.gateway.jz.service.SignatureFacade;
import com.iboxpay.settlement.gateway.jz.service.query.QueryPayment_500501;

/**
 * 单笔代扣接口
 * @author caolipeng
 * @date 2015年8月3日 下午2:30:26
 * @Version 1.0
 */
@Service
public class Payment_500201 extends AbstractPayment{
	
	private static Logger logger = LoggerFactory.getLogger(Payment_500201.class);
	public static final String ACCOUNT_VERIFY_CODE = "500101";
	public static final String BANK_TRANS_CODE = "500201";
	public static final String TRANS_DESC = "单笔代扣";
	private static final String VERIFY_SYS_NAME = "jzdk";
	
	@Resource
	private AccountVerifyService accountVerifyService;
	
	@Override
	public PaymentNavigation navigate() {
		return PaymentNavigation.create()
				.setBatchSize(1)//单笔
				.setDiffBank(true)//跨行
				.setSameBank(true)
				.setToPrivate(true)
				.setToCompany(true);//对公,对私都支持
	}
	
	@Override
	public String check(PaymentEntity[] payments) {
		return null;
	}

	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment_500501.class;
	}

	@Override
	public String getBankTransCode() {
		return BANK_TRANS_CODE;
	}

	@Override
	public String getBankTransDesc() {
		return "单笔代扣";
	}
	
	@Override
	public Map<String, String> getHeaderMap() {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		return headerMap;
	}
	
	@Override
	protected String getUri() {
		JzFrontEndConfig config = (JzFrontEndConfig)TransContext.getContext().getFrontEndConfig();
		return config.getUri().getVal();
	}
	//重写生成订单号
	@Override
	public void genBankBatchSeqId(PaymentEntity[] payments) {
		String orderId = Sequence.genNumberSequence(20);//生成20位固定长度订单号
		for (PaymentEntity paymentEntity : payments) {
			paymentEntity.setBankBatchSeqId(orderId);
		}
	}
	
	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
		String packStr = "";
		TransContext context = TransContext.getContext();
		JzAccountEntityExt  account = (JzAccountEntityExt)context.getMainAccount();
		PaymentEntity payment = payments[0];
		String merId = account.getMerchantId().getVal();//账号当做商户编号
		String orderId = payment.getSeqId();//订单号(固定20位)
		String transTime = DateTimeUtil.format(new Date(),Constants.DATE_FORMAT);
		
		AccountVerifyEntity accountVerifyEntity = (AccountVerifyEntity) context.getParameter("accountVerifyEntity" + payment.getId());
		if(accountVerifyEntity != null && StringUtils.isNotBlank(accountVerifyEntity.getBankSeqId())) {
			// 交易时间｜卡序列号(卡序列号传送过来)｜交易金额｜交易描述(msg需加密)
			String amount = payment.getAmount().toString().replace(".", "");
			String amt = StringUtils.addZeroToString(amount, 15, true);
			String msgInfo = transTime + Constants.INTERNAL_SEPARATOR + accountVerifyEntity.getBankSeqId() + Constants.INTERNAL_SEPARATOR + amt + Constants.INTERNAL_SEPARATOR + TRANS_DESC;
	
			logger.info(("【单笔代扣】报文:merId=" + merId + ",orderId=" + orderId + ",tradeCode=" + BANK_TRANS_CODE + ",msgInfo=" + msgInfo));
	
			StringBuffer sb = CommonPacker.packHeader(merId, BANK_TRANS_CODE, orderId);
			msgInfo = SignatureFacade.encrypt(msgInfo);//msg加密
			String signature = SignatureFacade.juSignature(merId + orderId + BANK_TRANS_CODE + msgInfo);//签名
			sb.append("msg=" + msgInfo).append(Constants.ELEMENT_SEPARATOR).append("signature=" + signature);
	
			packStr = sb.toString();
		}
		
		return packStr;
	}
	
	@Override
    public void pay(PaymentEntity[] payments) throws BaseTransException {
		StringBuilder ids = new StringBuilder();
        for (int i = 0; i < payments.length; i++) {
            if (i > 0) ids.append(",");
            ids.append(payments[i].getId());
        }
        
        AccountVerifyEntity accountVerifyEntity = verifyCard(payments[0]);
    	if(accountVerifyEntity != null && StringUtils.isNotBlank(accountVerifyEntity.getBankSeqId())) {
    		long ct = accountVerifyEntity.getCreateTime().getTime();
    		long nt = System.currentTimeMillis();
    		
    		try {
	    		if(nt - ct < 10000) {
	    			Thread.sleep(10000);
	    		}
	    		
	    		TransContext context = TransContext.getContext();
	    		context.setParameter("accountVerifyEntity" + payments[0].getId(), accountVerifyEntity);
    		
                String rsqt = pack(payments);
                logger.info("准备【支付】报文(paymentId:[" + ids + "], batchSeqId:" + payments[0].getBatchSeqId() + "): \n" + rsqt);
                openConnection();
                OutputStream os = getOutputStream();
                try {
                    //发送请求
                    send(os, rsqt);

                    handleAfterWrite(os);
                    // 获取输入流
                    InputStream is = getInputStream();
                    // 接收报文
                    String rsp = read(is);
                    logger.info("接收【支付】返回报文(paymentId:[" + ids + "], batchSeqId:" + payments[0].getBatchSeqId() + "): \n" + rsp);
                    handleAfterRead(is);
//                    // 解析报文
//                	String rsp = "{\"respCode\":\"00000\",\"respInfo\":null,\"cardSeqId\":\"000000000108\",\"feeAmt\":\"000000000000060\",\"transAmt\":\"000000000000200\",\"signature\":\"5d820dc2651db3fe2cff6eba007d0319\"}";
                    parse(rsp, payments);
                } catch (Throwable e) {
                    logger.error("", e);
                    PaymentStatus.processExceptionWhenPay(e, payments);
                }
            } catch (Throwable e) {
            	logger.info("准备【支付】报文(paymentId:[" + ids + "], batchSeqId:" + payments[0].getBatchSeqId() + "): \n" + e);
                PaymentStatus.processExceptionBeforePay(e, payments);
            } finally {
                closeConnection();
            }
    	}
    	else {
    		PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", accountVerifyEntity.getErrorCode(), accountVerifyEntity.getErrorMsg());
    	}
	}

	@Override
	public void parse(String respStr, PaymentEntity[] payments)
			throws ParseMessageException {
		TransContext context = TransContext.getContext();
		String charset = context.getCharset();//获取前置机配置的字符集
		try {
			/**
			 * {"respCode":"00000","respInfo":null,"cardSeqId":"000000000108","feeAmt":"000000000000060",
			 * "transAmt":"000000000000200","signature":"5d820dc2651db3fe2cff6eba007d0319"}
			 */
			Map map = (Map) JsonUtil.jsonToObject(respStr, charset, Map.class);
			String respCode = (String)map.get("respCode");
			String respInfo = (String)map.get("respInfo");
			if(Constants.SUCCESS.equals(respCode)){//返回”00000“
				//TODO 对方说是交易成功，待测试确定
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "", respCode, respInfo);
			} else if(Constants.ONE_FAIL.equals(respCode.substring(0, 1))){//TODO 返回"1"开头的时候，失败
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", respCode, respInfo);
			} else {//其它情况通过查询接口确定
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", respCode, respInfo);
			}
		} catch (IOException e) {
			throw new ParseMessageException("解析返回JSON报文异常:"+e.getMessage());
		}
	}
	
	/**
	 * 验卡
	 * @param payment
	 * @return
	 * @throws PackMessageException
	 */
	public AccountVerifyEntity verifyCard(PaymentEntity payment) throws PackMessageException {
		TransContext context = TransContext.getContext();
		JzAccountEntityExt  account = (JzAccountEntityExt)context.getMainAccount();
		String merId = account.getMerchantId().getVal();//账号当做商户编号
		String orderId = Sequence.genNumberSequence(20);//生成20位固定长度订单号
		String transTime = DateTimeUtil.format(new Date(),Constants.DATE_FORMAT);
		String accNo = payment.getCustomerAccNo();
		String accName = payment.getCustomerAccName();
		String certNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_CertNo);
		String mobileNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_MOBILENO);
		certNo = StringUtils.isBlank(certNo) ? "" : certNo;
		mobileNo = StringUtils.isBlank(mobileNo) ? "" : mobileNo;
		String respCode = null;
		String respInfo = null;
		String cardSeqId = null;
		int status = VerifyStatus.STATUS_UNKNOWN;
		AccountVerifyEntity accountVerifyEntity = accountVerifyService.getAccountVerifyEntity(payment.getAccNo(), accNo, accName, certNo, mobileNo);
		//判断是否验证通过
		if(accountVerifyEntity == null || StringUtils.isBlank(accountVerifyEntity.getSeqId()) || (!Constants.SUCCESS.equals(accountVerifyEntity.getErrorCode()))) {
			String msgInfo = transTime + Constants.INTERNAL_SEPARATOR + accName + Constants.INTERNAL_SEPARATOR + accNo + Constants.INTERNAL_SEPARATOR + "00" + Constants.INTERNAL_SEPARATOR + certNo + "|1|||" + mobileNo;
			msgInfo = SignatureFacade.encrypt(msgInfo);//msg加密
			String signature = SignatureFacade.juSignature(merId + orderId + ACCOUNT_VERIFY_CODE + msgInfo);//签名
			
			StringBuffer sbvf = CommonPacker.packHeader(merId, ACCOUNT_VERIFY_CODE, orderId);
			sbvf.append("msg=" + msgInfo).append(Constants.ELEMENT_SEPARATOR).append("signature=" + signature);
			
	        try {
	            String rsqt = sbvf.toString();
	            logger.info("准备【验卡】报文(paymentId:[" + payment.getId() + "], batchSeqId:" + payment.getBatchSeqId() + "): \n" + rsqt);
	            openConnection();
	            OutputStream os = getOutputStream();
	            try {
	                //发送请求
	                send(os, rsqt);

	                handleAfterWrite(os);
	                // 获取输入流
	                InputStream is = getInputStream();
	                // 接收报文
	                String rsp = read(is);
	                logger.info("接收【验卡】返回报文(paymentId:[" + payment.getId() + "], batchSeqId:" + payment.getBatchSeqId() + "): \n" + rsp);
	                handleAfterRead(is);
	                // 解析报文
	        		String charset = context.getCharset();//获取前置机配置的字符集
	                Map map = (Map) JsonUtil.jsonToObject(rsp, charset, Map.class);
	    			respCode = (String)map.get("respCode");
	    			respInfo = (String)map.get("respInfo");
	    			cardSeqId = (String)map.get("cardSeqId");
	    			if(Constants.SUCCESS.equals(respCode)) {
						status = VerifyStatus.STATUS_SUCCESS;
					}
					else {
						status = VerifyStatus.STATUS_FAIL;
					}
	            } catch (Throwable e) {
	                logger.error("", e);
	            }
	        } catch (Throwable e) {
	            logger.error("", e);
	        } finally {
	            closeConnection();
	        }
			
			if(accountVerifyEntity == null) {
				accountVerifyEntity = new AccountVerifyEntity();
				accountVerifyEntity.setStatus(status);
				accountVerifyEntity.setCustomerAccNo(accNo);
				accountVerifyEntity.setCustomerAccName(accName);
				accountVerifyEntity.setCertNo(certNo);
				accountVerifyEntity.setMobileNo(mobileNo);
				accountVerifyEntity.setBankSeqId(cardSeqId);
				accountVerifyEntity.setErrorCode(respCode);
				accountVerifyEntity.setErrorMsg(respInfo);
				accountVerifyEntity.setSysName(payment.getAccNo());
				accountVerifyEntity.setCreateTime(new Date());
				accountVerifyEntity.setUpdateTime(new Date());
				
				accountVerifyService.saveAccountVerify(accountVerifyEntity);
			}
			else {
				accountVerifyEntity.setStatus(status);
				accountVerifyEntity.setBankSeqId(cardSeqId);
				accountVerifyEntity.setErrorCode(respCode);
				accountVerifyEntity.setErrorMsg(respInfo);
				accountVerifyEntity.setUpdateTime(new Date());
				accountVerifyService.updateAccountVerify(accountVerifyEntity);
			}
		}
		
		return accountVerifyEntity;
	}
	
}
