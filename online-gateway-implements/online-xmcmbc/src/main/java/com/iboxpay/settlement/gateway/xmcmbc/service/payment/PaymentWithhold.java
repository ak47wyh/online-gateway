package com.iboxpay.settlement.gateway.xmcmbc.service.payment;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;
import com.iboxpay.settlement.gateway.common.service.AccountVerifyService;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.VerifyStatus;
import com.iboxpay.settlement.gateway.common.trans.callback.ICallBackPayment;
import com.iboxpay.settlement.gateway.common.trans.close.IClosePayment;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.util.DomUtil;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.xmcmbc.Constants;
import com.iboxpay.settlement.gateway.xmcmbc.SocketDiffHelper;
import com.iboxpay.settlement.gateway.xmcmbc.SocketHelper;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcFrontEndConfig;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcHelper;
import com.iboxpay.settlement.gateway.xmcmbc.service.BankNoHelper;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonPacker;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser.HeadInfo;
import com.iboxpay.settlement.gateway.xmcmbc.service.RSAHelper;
import com.iboxpay.settlement.gateway.xmcmbc.service.query.QueryPayment;

/**
 * 民生银行厦门分行-本行实时代扣
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
@Service
public class PaymentWithhold extends ConnectionAdapter implements IPayment {

	private static Logger logger = LoggerFactory.getLogger(PaymentWithhold.class);
	private static final String BANK_SAME_TRANS_CODE = "1009";
	private static final String BANK_DIFF_TRANS_CODE = "1003";
	private static final String BANK_VERIFY_CODE = "1004";
	private static final String BANK_WHITE_LIST_CODE = "1007";
	private static final String VERIFY_SYS_NAME = "xmcmbc";

	@Resource
	private AccountVerifyService accountVerifyService;

	@Override
	public PaymentNavigation navigate() {
		return PaymentNavigation.create()//由于厦门民生直连 不支持对公,故直接不支持对公
				.setToPrivate(true).setDiffBank(true).setSameBank(true).setPriority(3)//单笔优先级高点吧
				.setBatchSize(1);//单笔实时
	}

	@Override
	public String check(PaymentEntity[] payments) {
		return null;
	}

	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment.class;
	}

	@Override
	public String getBankTransCode() {
		return BANK_SAME_TRANS_CODE;
	}

	@Override
	public String getBankTransDesc() {
		return "厦门民生实时代扣";
	}

	public byte[] pack(PaymentEntity[] payments) throws PackMessageException {
		byte[] proxyBytes = null;
		PaymentEntity payment = payments[0];
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig) context.getFrontEndConfig();
		File privateKeyFile = config.getPrivateKeyFile().getFileVal();
		File publicKeyFile = config.getPublicKeyFile().getFileVal();
		String poxy = config.getPoxy().getVal();
		String charsetName = config.getCharset().getVal();
		/** 证件类型*/
		String certType = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_CertType);
		certType = XmcmbcHelper.initCertType(certType);
		/** 证件号码*/
		String certNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_CertNo);
		String mobileNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_MOBILENO);
		String tranId = payment.getSeqId();
		try {
			if (XmcmbcHelper.isToSameBack(payment.getCustomerBankFullName())) {//本行
				Element root = CommonPacker.packHeader();
				DomUtil.addChild(root, "TRAN_ID", tranId);//渠道流水号,唯一
				DomUtil.addChild(root, "BUSI_TYPE", "");//业务类型
				DomUtil.addChild(root, "BUSI_NO", "");//业务号码
				DomUtil.addChild(root, "CURRENCY", Constants.CURRENCY);//币种，固定值RMB
				DomUtil.addChild(root, "ACC_NO", payment.getCustomerAccNo());//付款人账户号
				DomUtil.addChild(root, "ACC_NAME", payment.getCustomerAccName());//付款人账户名
				DomUtil.addChild(root, "PAYER_PHONE", mobileNo);//付款人手机号
				BigDecimal amount = payment.getAmount();
				amount = amount.multiply(Constants.MULTI_100);
				DomUtil.addChild(root, "TRANS_AMT", amount.setScale(0, RoundingMode.HALF_DOWN).toString());//交易金额,单位为分
				DomUtil.addChild(root, "CHK_FLAG", "2");//认证检查标志 2-户名且证件：必须有证件信息且户名、证件与系统记录必须相符，才能入账
				DomUtil.addChild(root, "CERT_TYPE", certType);//证件类型
				DomUtil.addChild(root, "CERT_NO", certNo);//证件号码
				DomUtil.addChild(root, "REMARK", "代扣");//客户流水摘要
				DomUtil.addChild(root, "RESV", "");//备用域
				String xml = DomUtil.documentToString(root.getDocument(), charsetName);
				logger.info("准备【支付】报文(paymentId:[{}], batchSeqId:{})", payments[0].getId(), payments[0].getBatchSeqId());
				logger.info(xml);
				byte[] bodyBytes = xml.getBytes(charsetName);
				RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
				byte[] signData = RSAHelper.signRSA(bodyBytes, false, charsetName);
				byte[] encrtptData = RSAHelper.encryptRSA(bodyBytes, false, charsetName);

				byte[] packBytes = null;
				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(config.gettCompanyId().getVal(), 15, " ").getBytes(charsetName));//15位合作方编号，位数不足左补空格
				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(BANK_SAME_TRANS_CODE, 8, " ").getBytes(charsetName));//8位交易码，位数不足左补空格
				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad("256", 4, "0").getBytes(charsetName));//4位签名域长度，右对齐左补零
				packBytes = ArrayUtils.addAll(packBytes, signData);//签名域值
				packBytes = ArrayUtils.addAll(packBytes, encrtptData);//XML报文数据主体密文

				byte[] bytes = null;
				bytes = ArrayUtils.addAll(bytes, StringUtils.leftPad(String.valueOf(packBytes.length), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
				bytes = ArrayUtils.addAll(bytes, packBytes);

				// 与代理定义的协议
				if ("true".equals(poxy)) {
					int len = bytes.length + 4 + tranId.getBytes(charsetName).length;
					proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(len), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
					proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(tranId.getBytes(charsetName).length), 4, "0").getBytes(charsetName));//4位订单号长度，右对齐左补零
					proxyBytes = ArrayUtils.addAll(proxyBytes, tranId.getBytes(charsetName));
					proxyBytes = ArrayUtils.addAll(proxyBytes, bytes);
				} else {
					proxyBytes = bytes;
				}
			} else {// 跨行
				Element root = CommonPacker.diffPackHeader();
				DomUtil.addChild(root, "SerialNo", tranId);//渠道流水号,唯一
				DomUtil.addChild(root, "MerId", "");//商户号
				DomUtil.addChild(root, "MerName", "");//商户名（机构名称）
				DomUtil.addChild(root, "BizType", "14900");//业务类型
				/** 付款账户类型 00-对私（默认）01-对公 */
				if (payment.getCustomerAccType() == 1) {//对公
					DomUtil.addChild(root, "BizObjType", "01");
				} else {
					DomUtil.addChild(root, "BizObjType", "00");
				}
				DomUtil.addChild(root, "PayerAcc", payment.getCustomerAccNo());//收款人账户号
				DomUtil.addChild(root, "PayerName", payment.getCustomerAccName());//收款人账户名
				/**
				 * 卡折标志 0-	借记卡（默认）1-存折2-贷记卡（信用卡）3-公司账号
				 */
				if (payment.getCustomerCardType() == 1) {
					DomUtil.addChild(root, "CardType", "1");
				} else if (payment.getCustomerCardType() == 2) {
					DomUtil.addChild(root, "CardType", "2");
				} else if (payment.getCustomerCardType() == 3) {
					DomUtil.addChild(root, "CardType", "3");
				} else {
					DomUtil.addChild(root, "CardType", "0");
				}
				//收款人账户开户行名称,给到具体支行联行行号就行
				DomUtil.addChild(root, "PayerBankName", payment.getCustomerBankFullName());
				String bankFullName = payment.getCustomerBankFullName();
				String bankNo = BankNoHelper.convertBankNo(bankFullName);
				DomUtil.addChild(root, "PayerBankInsCode", bankNo);//付款行银联机构号
				DomUtil.addChild(root, "PayerPhone", mobileNo);//付款人手机号码
				BigDecimal amount = payment.getAmount();
				amount = amount.multiply(Constants.MULTI_100);
				DomUtil.addChild(root, "TranAmt", amount.setScale(0, RoundingMode.HALF_DOWN).toString());//交易金额,单位为分
				DomUtil.addChild(root, "Currency", Constants.CURRENCY);//币种，固定值RMB
				DomUtil.addChild(root, "CertType", certType);//证件类型
				DomUtil.addChild(root, "CertNo", certNo);//证件号码
				DomUtil.addChild(root, "ProvNo", "440000");//付款省份编码
				DomUtil.addChild(root, "Purpose", "保险代扣");//用途说明
				DomUtil.addChild(root, "Postscript", "");//附言说明
				String xml = DomUtil.documentToString(root.getDocument(), charsetName);
				logger.info("准备【支付】报文(paymentId:[{}], batchSeqId:{})", payments[0].getId(), payments[0].getBatchSeqId());
				logger.info(xml);
				byte[] bodyBytes = xml.getBytes(charsetName);
				RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
				byte[] signData = RSAHelper.signRSA(bodyBytes, false, charsetName);
				byte[] encrtptData = RSAHelper.encryptRSA(bodyBytes, false, charsetName);

				byte[] packBytes = null;
				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(config.getCompanyId().getVal(), 8, " ").getBytes(charsetName));//8位合作方编号，位数不足左补空格
				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(BANK_DIFF_TRANS_CODE, 8, " ").getBytes(charsetName));//8位交易码，位数不足左补空格
				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad("256", 4, "0").getBytes(charsetName));//4位签名域长度，右对齐左补零
				packBytes = ArrayUtils.addAll(packBytes, signData);//签名域值
				packBytes = ArrayUtils.addAll(packBytes, encrtptData);//XML报文数据主体密文
				byte[] bytes = null;
				bytes = ArrayUtils.addAll(bytes, StringUtils.leftPad(String.valueOf(packBytes.length), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
				bytes = ArrayUtils.addAll(bytes, packBytes);

				// 与代理定义的协议
				if ("true".equals(poxy)) {
					int len = bytes.length + 4 + tranId.getBytes(charsetName).length;
					proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(len), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
					proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(tranId.getBytes(charsetName).length), 4, "0").getBytes(charsetName));//4位订单号长度，右对齐左补零
					proxyBytes = ArrayUtils.addAll(proxyBytes, tranId.getBytes(charsetName));
					proxyBytes = ArrayUtils.addAll(proxyBytes, bytes);
				} else {
					proxyBytes = bytes;
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new PackMessageException("不支持字符编码【" + charsetName + "】", e);
		} catch (Exception e) {
			throw new PackMessageException("发生异常:" + e);
		}

		return proxyBytes;
	}

	public void parse(String resp, PaymentEntity[] payments) throws ParseMessageException {
		//返回的应答报文:decrypted: <?xml version="1.0" encoding="UTF-8"?><TRAN_RESP><RESP_TYPE>S</RESP_TYPE><RESP_CODE>000000</RESP_CODE><RESP_MSG>交易成功</RESP_MSG><MCHNT_CD>201511230000042</MCHNT_CD><TRAN_DATE>20160229</TRAN_DATE><TRAN_TIME>153623</TRAN_TIME><TRAN_ID>4jz00006pq</TRAN_ID><BANK_TRAN_ID>2016022909786628</BANK_TRAN_ID><BANK_TRAN_DATE>20160229</BANK_TRAN_DATE><BANK_TRAN_TIME>154154</BANK_TRAN_TIME><CHARGE_FEE>0</CHARGE_FEE><RESV></RESV></TRAN_RESP>
		try {
			Element root = DomUtil.parseXml(resp);
			if (XmcmbcHelper.isToSameBack(payments[0].getCustomerBankFullName())) {//本行
				HeadInfo headInfo = CommonParser.parseHead(root);
				String tranId = DomUtil.getTextTrim(root, "TRAN_ID");//原交易流水
				if (tranId.equalsIgnoreCase(payments[0].getSeqId())) {//防止返回的报文不是对应的支付报文
					if (Constants.RESPONSE_CODE_SUCCESS.equals(headInfo.code)) {//直接成功
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "交易成功", headInfo.code, headInfo.message);
					} else if (XmcmbcHelper.isFail(headInfo.code)) {//直接失败
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "交易失败", headInfo.code, headInfo.message);
					} else {//未确定的，查询确定
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", headInfo.code, headInfo.message);
					}
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", "", "");
				}
			} else {
				HeadInfo headInfo = CommonParser.diffParseHead(root);
				String qeqSerialNo = DomUtil.getTextTrim(root, "ReqSerialNo");//原交易流水
				if (qeqSerialNo.equalsIgnoreCase(payments[0].getSeqId())) {//防止返回的报文不是对应的支付报文
					if (Constants.RESPONSE_CODE_SUCCESS.equals(headInfo.code)) {//直接成功
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "交易成功", headInfo.code, headInfo.message);
					} else if (XmcmbcHelper.isFail(headInfo.code)) {//直接失败
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "交易失败", headInfo.code, headInfo.message);
					} else {//未确定的，查询确定
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", headInfo.code, headInfo.message);
					}
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", "", "");
				}
			}
		} catch (Exception e) {
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "代扣响应报文体解密异常,查询确认", "", "");
			throw new ParseMessageException("解密发生异常:" + e);
		}
	}

	@Override
	public TransCode getTransCode() {
		return TransCode.PAY;
	}

	@Override
	public boolean navigateMatch(PaymentEntity payment) {
		return true;
	}

	@Override
	public void genBankBatchSeqId(PaymentEntity[] payments) {
		String bankBatchSeqId = Sequence.genSequence();
		for (PaymentEntity payment : payments) {
			payment.setBankBatchSeqId(bankBatchSeqId);
		}
	}

	@Override
	public void genBankSeqId(PaymentEntity[] payments) {
		//do nothing
	}

	@Override
	public void pay(PaymentEntity[] payments) throws BaseTransException {
		StringBuilder ids = new StringBuilder();
		for (int i = 0; i < payments.length; i++) {
			if (i > 0) ids.append(",");
			ids.append(payments[i].getId());
		}
		byte[] rsqt = pack(payments);
		String rspStr = null;
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String ip = config.getIp().getVal();
		int port = config.getPort().getIntVal();
		int diffPort = config.getDiffPort().getIntVal();
		int timeout = config.getTimeout().getIntVal();
		String poxy = config.getPoxy().getVal();
		String charsetName = config.getCharset().getVal();
		String tranId = payments[0].getSeqId();
		if (XmcmbcHelper.isToSameBack(payments[0].getCustomerBankFullName())) {// 本行 socket短链接
			if ("false".equals(poxy)) {
				try {
					logger.info("开始发送【支付】报文到本行长连接队列");
					SocketHelper.config = config;
					SocketHelper.put(tranId, rsqt);
					SocketHelper.countDownMap.get(tranId).await(timeout, TimeUnit.SECONDS);
					rspStr = SocketHelper.rspMap.get(tranId);
				} catch (Exception e) {
					logger.error("", e);
				} finally {
					SocketHelper.clear(tranId);
				}
			} else {
				openConnection(ip, port);
				OutputStream os = getOutputStream();
				try {
					//发送请求
					send(os, rsqt);
					handleAfterWrite(os);
					// 获取输入流
					InputStream is = getInputStream();
					// 接收报文
					byte[] bytes = CommonParser.readByte(is);

					handleAfterRead(is);

					if (bytes != null) {
						// 对密文解密
						File privateKeyFile = config.getPrivateKeyFile().getFileVal();
						File publicKeyFile = config.getPublicKeyFile().getFileVal();
						RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
						byte[] decryptedBytes = RSAHelper.decryptRSA(bytes, false, charsetName);
						rspStr = new String(decryptedBytes, charsetName);
					}
				} catch (Throwable e) {
					logger.error("", e);
					PaymentStatus.processExceptionWhenPay(e, payments);
				}
			}
		} else {// 跨行socket全双工异步长连接
				// 首次发送代扣需要实名认证和白名单采集
			AccountVerifyEntity accountVerify = certification(payments[0]);
			if (accountVerify != null
					&& (Constants.RESPONSE_EXECCODE_SUCCESS.equals(accountVerify.getVerifyErrorCode()) || Constants.RESPONSE_EXECCODE_REPEAT.equals(accountVerify.getVerifyErrorCode()))) {
				if ("false".equals(poxy)) {
					try {
						logger.info("开始发送【支付】报文到跨行长连接队列");
						SocketDiffHelper.config = config;
						SocketDiffHelper.put(tranId, rsqt);
						SocketDiffHelper.countDownMap.get(tranId).await(timeout, TimeUnit.SECONDS);
						rspStr = SocketDiffHelper.rspMap.get(tranId);
					} catch (Exception e) {
						logger.error("", e);
					} finally {
						SocketDiffHelper.clear(tranId);
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

						if (bytes != null) {
							// 对密文解密
							File privateKeyFile = config.getPrivateKeyFile().getFileVal();
							File publicKeyFile = config.getPublicKeyFile().getFileVal();
							RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
							byte[] decryptedBytes = RSAHelper.decryptRSA(bytes, false, charsetName);
							rspStr = new String(decryptedBytes, charsetName);
						}
					} catch (Throwable e) {
						logger.error("", e);
						PaymentStatus.processExceptionWhenPay(e, payments);
					}
				}
			} else {
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "交易失败", "", "实名认证不通过");
				return;
			}
		}

		if (StringUtils.isBlank(rspStr)) {
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", "未正常返回结果", "");
			return;
		}

		logger.info("接收【支付】返回报文(paymentId:[{}], batchSeqId:{}): \n{}", ids.toString(), payments[0].getBatchSeqId(), rspStr);
		// 解析报文
		parse(rspStr, payments);
	}

	/**
	 * 跨行账号验证认证
	 * @param payment
	 * @param accountVerify
	 * @return
	 * @throws PackMessageException
	 */
	private AccountVerifyEntity certification(PaymentEntity payment) throws PackMessageException {
		String accNo = payment.getCustomerAccNo();
		String accName = payment.getCustomerAccName();
		/** 证件类型*/
		String certType = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_CertType);
		certType = XmcmbcHelper.initCertType(certType);
		/** 证件号码*/
		String certNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_CertNo);
		// 手机号
		String mobileNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_MOBILENO);
		AccountVerifyEntity accountVerify = accountVerifyService.getAccountVerifyEntity(payment.getAccNo(), accNo, accName, certNo, mobileNo);
		// 实名认证
		if (accountVerify == null || (accountVerify != null && VerifyStatus.STATUS_SUCCESS != accountVerify.getStatus())) {
			accountVerify = verify(payment, accountVerify);
		}

		// 白名单采集
		if (accountVerify != null && VerifyStatus.STATUS_SUCCESS == accountVerify.getStatus()
				&& !(Constants.RESPONSE_EXECCODE_SUCCESS.equals(accountVerify.getVerifyErrorCode()) || Constants.RESPONSE_EXECCODE_REPEAT.equals(accountVerify.getVerifyErrorCode()))) {
			accountVerify = whiteList(payment, accountVerify);
		}

		return accountVerify;
	}

	/**
	 * 实名认证
	 * @param payment
	 * @return
	 * @throws PackMessageException
	 */
	private AccountVerifyEntity verify(PaymentEntity payment, AccountVerifyEntity accountVerify) throws PackMessageException {
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig) context.getFrontEndConfig();
		String charsetName = config.getCharset().getVal();
		File privateKeyFile = config.getPrivateKeyFile().getFileVal();
		File publicKeyFile = config.getPublicKeyFile().getFileVal();
		String ip = config.getIp().getVal();
		int diffPort = config.getDiffPort().getIntVal();
		int timeout = config.getTimeout().getIntVal() * 1000;
		String poxy = config.getPoxy().getVal();

		String accNo = payment.getCustomerAccNo();
		String accName = payment.getCustomerAccName();
		/** 证件类型*/
		String certType = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_CertType);
		certType = XmcmbcHelper.initCertType(certType);
		/** 证件号码*/
		String certNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_CertNo);
		// 手机号
		String mobileNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_MOBILENO);

		//判断是否验证通过
		try {
			byte[] bytes = null;
			Element root = CommonPacker.diffPackHeader();
			String serialNo = Sequence.genSequence();
			DomUtil.addChild(root, "SerialNo", serialNo);//渠道流水号,唯一
			//DomUtil.addChild(root, "MerId", "");//商户号
			//DomUtil.addChild(root, "MerName", "");//商户名（机构名称）
			/**
			 * 卡折标志 0-	借记卡（默认）1-存折2-贷记卡（信用卡）3-公司账号
			 */
			if (payment.getCustomerCardType() == 1) {
				DomUtil.addChild(root, "CardType", "1");
			} else if (payment.getCustomerCardType() == 2) {
				DomUtil.addChild(root, "CardType", "2");
			} else if (payment.getCustomerCardType() == 3) {
				DomUtil.addChild(root, "CardType", "3");
			} else {
				DomUtil.addChild(root, "CardType", "0");
			}

			DomUtil.addChild(root, "AccNo", payment.getCustomerAccNo());//账户号
			DomUtil.addChild(root, "AccName", payment.getCustomerAccName());//账户名
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
			packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(BANK_VERIFY_CODE, 8, " ").getBytes(charsetName));//8位交易码，位数不足左补空格
			packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad("256", 4, "0").getBytes(charsetName));//4位签名域长度，右对齐左补零
			packBytes = ArrayUtils.addAll(packBytes, signData);//签名域值
			packBytes = ArrayUtils.addAll(packBytes, encrtptData);//XML报文数据主体密文

			bytes = ArrayUtils.addAll(bytes, StringUtils.leftPad(String.valueOf(packBytes.length), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
			bytes = ArrayUtils.addAll(bytes, packBytes);

			byte[] proxyBytes = null;
			// 与代理定义的协议
			if ("true".equals(poxy)) {
				int len = bytes.length + 4 + serialNo.getBytes(charsetName).length;
				proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(len), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
				proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(serialNo.getBytes(charsetName).length), 4, "0").getBytes(charsetName));//4位订单号长度，右对齐左补零
				proxyBytes = ArrayUtils.addAll(proxyBytes, serialNo.getBytes(charsetName));
				proxyBytes = ArrayUtils.addAll(proxyBytes, bytes);
			} else {
				proxyBytes = bytes;
			}

			String respCode = null;
			String respMsg = null;
			String validateStatus = null;
			String rspStr = null;
			// 实名认证
			if ("false".equals(poxy)) {
				try {
					logger.info("开始发送【实名认证】报文到跨行长连接队列");
					SocketDiffHelper.config = config;
					SocketDiffHelper.put(serialNo, proxyBytes);
					SocketDiffHelper.countDownMap.get(serialNo).await(timeout, TimeUnit.SECONDS);
					rspStr = SocketDiffHelper.rspMap.get(serialNo);
				} catch (Exception e) {
					throw new PackMessageException("实名认证发送请求发生异常:" + e);
				} finally {
					SocketDiffHelper.clear(serialNo);
				}
			} else {
				openConnection(ip, diffPort);
				OutputStream os = getOutputStream();
				try {
					//发送请求
					send(os, proxyBytes);
					handleAfterWrite(os);
					// 获取输入流
					InputStream is = getInputStream();
					// 接收报文
					byte[] rsp = CommonParser.readDiffByte(is);

					handleAfterRead(is);
					byte[] decryptedBytes = RSAHelper.decryptRSA(rsp, false, charsetName);
					rspStr = new String(decryptedBytes, charsetName);
				} catch (Throwable e) {
					throw new PackMessageException("实名认证发送请求发生异常:" + e);
				}
			}

			int status = VerifyStatus.STATUS_UNKNOWN;
			if (StringUtils.isNotBlank(rspStr)) {
				logger.info("接收【实名认证】返回报文: \n" + rspStr);
				Element rootRsp = DomUtil.parseXml(rspStr);
				HeadInfo headInfo = CommonParser.diffRspHead(rootRsp);
				respCode = headInfo.code;
				respMsg = headInfo.message;
				validateStatus = DomUtil.getTextTrim(rootRsp, "ValidateStatus");//认证状态 00-认证成功 99-认证失败
				if (Constants.RESPONSE_VALIDATE_SUCCESS.equals(validateStatus)) {
					status = VerifyStatus.STATUS_SUCCESS;
				} else {
					status = VerifyStatus.STATUS_FAIL;
				}
			}

			if (accountVerify == null) {
				accountVerify = new AccountVerifyEntity();
				accountVerify.setPayTransCode(TransCode.VERIFY.getCode());
				accountVerify.setStatus(status);
				accountVerify.setCustomerAccNo(accNo);
				accountVerify.setCustomerAccName(accName);
				accountVerify.setCertNo(certNo);
				accountVerify.setMobileNo(mobileNo);
				accountVerify.setSeqId(serialNo);
				accountVerify.setErrorCode(respCode);
				accountVerify.setErrorMsg(respMsg);
				accountVerify.setBankStatus(validateStatus);
				accountVerify.setSysName(payment.getAccNo());
				accountVerify.setCreateTime(new Date());
				accountVerify.setUpdateTime(new Date());

				accountVerifyService.saveAccountVerify(accountVerify);
			} else {
				accountVerify.setStatus(status);
				accountVerify.setSeqId(serialNo);
				accountVerify.setErrorCode(respCode);
				accountVerify.setErrorMsg(respMsg);
				accountVerify.setBankStatus(validateStatus);
				accountVerify.setUpdateTime(new Date());
				accountVerifyService.updateAccountVerify(accountVerify);
			}

		} catch (Exception e) {
			throw new PackMessageException("实名认证发生异常:" + e);
		}

		return accountVerify;
	}

	private AccountVerifyEntity whiteList(PaymentEntity payment, AccountVerifyEntity accountVerify) throws PackMessageException {
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig) context.getFrontEndConfig();
		String charsetName = config.getCharset().getVal();
		File privateKeyFile = config.getPrivateKeyFile().getFileVal();
		File publicKeyFile = config.getPublicKeyFile().getFileVal();
		String ip = config.getIp().getVal();
		int diffPort = config.getDiffPort().getIntVal();
		int timeout = config.getTimeout().getIntVal() * 1000;
		String poxy = config.getPoxy().getVal();

		String accNo = payment.getCustomerAccNo();
		String accName = payment.getCustomerAccName();
		/** 证件类型*/
		String certType = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_CertType);
		certType = XmcmbcHelper.initCertType(certType);
		/** 证件号码*/
		String certNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_CertNo);
		// 手机号
		String mobileNo = (String) payment.getExtProperty(PaymentEntity.EXT_PROPERTY_MOBILENO);

		try {
			byte[] bytes = null;
			Element root = CommonPacker.diffPackHeader();
			String serialNo = Sequence.genSequence();
			DomUtil.addChild(root, "SerialNo", serialNo);//渠道流水号,唯一
			//DomUtil.addChild(root, "MerId", "");//商户号
			//DomUtil.addChild(root, "MerName", "");//商户名（机构名称）
			String bankFullName = payment.getCustomerBankFullName();
			String bankNo = BankNoHelper.convertBankNo(bankFullName);
			DomUtil.addChild(root, "BankInsCode", bankNo);//付款行银联机构号
			DomUtil.addChild(root, "BankAccNo", accNo);//账户号
			DomUtil.addChild(root, "BankAccName", accName);//账户名
			/**
			 * 账号类型 0-借记卡（默认）1-存折 2-贷记卡（信用卡）3-公司账号
			 */
			if (payment.getCustomerCardType() == 1) {
				DomUtil.addChild(root, "BankAccType", "1");
			} else if (payment.getCustomerCardType() == 2) {
				DomUtil.addChild(root, "BankAccType", "2");
			} else if (payment.getCustomerCardType() == 3) {
				DomUtil.addChild(root, "BankAccType", "3");
			} else {
				DomUtil.addChild(root, "BankAccType", "0");
			}

			DomUtil.addChild(root, "CertType", certType);//证件类型
			DomUtil.addChild(root, "CertNo", certNo);//证件号码
			DomUtil.addChild(root, "Mobile", mobileNo);//手机号码

			String xml = DomUtil.documentToString(root.getDocument(), charsetName);
			logger.info("发送【白名单采集】报文: \n" + xml);
			byte[] bodyBytes = xml.getBytes(charsetName);
			RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
			byte[] signData = RSAHelper.signRSA(bodyBytes, false, charsetName);
			byte[] encrtptData = RSAHelper.encryptRSA(bodyBytes, false, charsetName);

			byte[] packBytes = null;
			packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(config.getCompanyId().getVal(), 8, " ").getBytes(charsetName));//8位合作方编号，位数不足左补空格
			packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(BANK_WHITE_LIST_CODE, 8, " ").getBytes(charsetName));//8位交易码，位数不足左补空格
			packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad("256", 4, "0").getBytes(charsetName));//4位签名域长度，右对齐左补零
			packBytes = ArrayUtils.addAll(packBytes, signData);//签名域值
			packBytes = ArrayUtils.addAll(packBytes, encrtptData);//XML报文数据主体密文

			bytes = ArrayUtils.addAll(bytes, StringUtils.leftPad(String.valueOf(packBytes.length), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
			bytes = ArrayUtils.addAll(bytes, packBytes);

			byte[] proxyBytes = null;
			// 与代理定义的协议
			if ("true".equals(poxy)) {
				int len = bytes.length + 4 + serialNo.getBytes(charsetName).length;
				proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(len), 8, "0").getBytes(charsetName));//8位报文总长度，右对齐左补零
				proxyBytes = ArrayUtils.addAll(proxyBytes, StringUtils.leftPad(String.valueOf(serialNo.getBytes(charsetName).length), 4, "0").getBytes(charsetName));//4位订单号长度，右对齐左补零
				proxyBytes = ArrayUtils.addAll(proxyBytes, serialNo.getBytes(charsetName));
				proxyBytes = ArrayUtils.addAll(proxyBytes, bytes);
			} else {
				proxyBytes = bytes;
			}

			String respCode = null;
			String rspStr = null;
			// 白名单采集
			if ("false".equals(poxy)) {
				try {
					logger.info("开始发送【白名单采集】报文到跨行长连接队列");
					SocketDiffHelper.config = config;
					SocketDiffHelper.put(serialNo, proxyBytes);
					SocketDiffHelper.countDownMap.get(serialNo).await(timeout, TimeUnit.SECONDS);
					rspStr = SocketDiffHelper.rspMap.get(serialNo);
				} catch (Exception e) {
					throw new PackMessageException("白名单采集发送请求发生异常:" + e);
				} finally {
					SocketDiffHelper.clear(serialNo);
				}
			} else {
				openConnection(ip, diffPort);
				OutputStream os = getOutputStream();
				try {
					//发送请求
					send(os, proxyBytes);
					handleAfterWrite(os);
					// 获取输入流
					InputStream is = getInputStream();
					// 接收报文
					byte[] rsp = CommonParser.readDiffByte(is);

					handleAfterRead(is);
					byte[] decryptedBytes = RSAHelper.decryptRSA(rsp, false, charsetName);
					rspStr = new String(decryptedBytes, charsetName);
				} catch (Throwable e) {
					throw new PackMessageException("白名单采集发送请求发生异常:" + e);
				}
			}

			if (StringUtils.isNotBlank(rspStr)) {
				logger.info("接收【白名单采集】返回报文: \n" + rspStr);
				Element rootRsp = DomUtil.parseXml(rspStr);
				HeadInfo headInfo = CommonParser.diffRspHead(rootRsp);
				respCode = headInfo.code;//000000 表示采集成功;	000022账号信息重复，请确认;两种都代表账号已存在白名单内
			}

			accountVerify.setSeqId(serialNo);
			accountVerify.setVerifyErrorCode(respCode);
			accountVerify.setUpdateTime(new Date());
			accountVerifyService.updateAccountVerify(accountVerify);
		} catch (Exception e) {
			throw new PackMessageException("白名单采集发生异常:" + e);
		}

		return accountVerify;
	}

	@Override
	public Class<? extends IRefundPayment> getRefundClass() {
		return null;
	}

	@Override
	public Class<? extends IRefundQueryPayment> getRefundQueryClass() {
		return null;
	}

	@Override
	public Class<? extends IReversePayment> getReverseClass() {
		return null;
	}

	@Override
	public Class<? extends IClosePayment> getCloseClass() {
		return null;
	}

	@Override
	public Class<? extends ICallBackPayment> getCallBackClass() {
		return null;
	}
}
