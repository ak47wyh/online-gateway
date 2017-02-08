package com.iboxpay.settlement.gateway.xmcmbc.service.query;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.DomUtil;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.xmcmbc.Constants;
import com.iboxpay.settlement.gateway.xmcmbc.SocketDiffHelper;
import com.iboxpay.settlement.gateway.xmcmbc.SocketHelper;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcFrontEndConfig;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcHelper;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonPacker;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser.HeadInfo;
import com.iboxpay.settlement.gateway.xmcmbc.service.RSAHelper;

/**
 * 实时代付-结果查询
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
@Service
public class QueryPayment extends ConnectionAdapter implements IQueryPayment {

	private static Logger logger = LoggerFactory.getLogger(QueryPayment.class);
	private static final String BANK_QUERY_CODE = "xmcmbc_query";
	private static final String BANK_SAME_QUERY_CODE = "3009";
	private static final String BANK_DIFF_QUERY_CODE = "3003";
	private static final String BANK_TRAN_ID_CODE = "XMCMBC_TRAN_ID";

	@Override
	public String getBankTransCode() {
		return BANK_QUERY_CODE;
	}

	@Override
	public String getBankTransDesc() {
		return "账务交易结果查询";
	}

	public byte[] pack(PaymentEntity[] payments, String tranId) throws PackMessageException {
		byte[] proxyBytes = null;
		PaymentEntity payment = payments[0];
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig) context.getFrontEndConfig();
		File privateKeyFile = config.getPrivateKeyFile().getFileVal();
		File publicKeyFile = config.getPublicKeyFile().getFileVal();
		String poxy = config.getPoxy().getVal();
		String charsetName = context.getCharset();
		try {
			if (XmcmbcHelper.isToSameBack(payment.getCustomerBankFullName())) {//本行
				Element root = CommonPacker.queryPackHeader();
				context.setParameter(BANK_TRAN_ID_CODE, tranId);
				DomUtil.addChild(root, "TRAN_ID", tranId);//查询交易流水,唯一
				//原来交易日期
				String oldTransDate = DateTimeUtil.format(payment.getTransDate(), Constants.TRANS_DATE_FORMAT);
				DomUtil.addChild(root, "ORI_TRAN_DATE", oldTransDate);
				DomUtil.addChild(root, "ORI_TRAN_ID", payment.getBankBatchSeqId());//原来交易流水
				DomUtil.addChild(root, "RESV", "");//备用域
				String xml = DomUtil.documentToString(root.getDocument(), charsetName);
				byte[] bodyBytes = xml.getBytes(charsetName);

				RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
				byte[] signData = RSAHelper.signRSA(bodyBytes, false, charsetName);
				byte[] encrtptData = RSAHelper.encryptRSA(bodyBytes, false, charsetName);

				byte[] packBytes = null;

				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(config.gettCompanyId().getVal(), 15, " ").getBytes(charsetName));//15位合作方编号，位数不足左补空格
				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(BANK_SAME_QUERY_CODE, 8, " ").getBytes(charsetName));//8位交易码，位数不足左补空格
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
			} else {
				Element root = CommonPacker.diffQueryPackHeader();
				DomUtil.addChild(root, "SerialNo", tranId);//查询交易流水,唯一
				//原来交易日期
				String oldTransDate = DateTimeUtil.format(payment.getTransDate(), Constants.TRANS_DATE_FORMAT);
				DomUtil.addChild(root, "OriTransDate", oldTransDate);
				DomUtil.addChild(root, "OriReqSerialNo", payment.getBankBatchSeqId());//原来交易流水
				DomUtil.addChild(root, "RESV", "");//备用域
				String xml = DomUtil.documentToString(root.getDocument(), charsetName);
				byte[] bodyBytes = xml.getBytes(charsetName);

				RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
				byte[] signData = RSAHelper.signRSA(bodyBytes, false, charsetName);
				byte[] encrtptData = RSAHelper.encryptRSA(bodyBytes, false, charsetName);

				byte[] packBytes = null;
				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(config.getCompanyId().getVal(), 8, " ").getBytes(charsetName));//8位合作方编号，位数不足左补空格
				packBytes = ArrayUtils.addAll(packBytes, StringUtils.leftPad(BANK_DIFF_QUERY_CODE, 8, " ").getBytes(charsetName));//8位交易码，位数不足左补空格
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
			if (StringUtils.isBlank(resp)) {
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", "未正常返回结果", "");
				return;
			}

			Element root = DomUtil.parseXml(resp);
			if (XmcmbcHelper.isToSameBack(payments[0].getCustomerBankFullName())) {//本行
				HeadInfo headInfo = CommonParser.parseHead(root);
				String tranId = DomUtil.getTextTrim(root, "ORI_TRAN_ID");//原交易流水
				String oriRespType = DomUtil.getTextTrim(root, "ORI_RESP_TYPE");//原交易响应类型
				String oriRespCode = DomUtil.getTextTrim(root, "ORI_RESP_CODE");//原交易响应代码
				String oriRespMsg = DomUtil.getTextTrim(root, "ORI_RESP_MSG");//原交易响应描述
				if (payments[0].getBankBatchSeqId().equalsIgnoreCase(tranId) && Constants.RESPONSE_TYPE_SUCCESS.equals(headInfo.type)) {//防止返回的报文不是对应的支付报文
					if (Constants.RESPONSE_TYPE_SUCCESS.equals(oriRespType) && Constants.RESPONSE_CODE_SUCCESS.equals(oriRespCode)) {//直接成功
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "交易成功", oriRespCode, oriRespMsg);
					} else if (XmcmbcHelper.isFail(oriRespCode)) {//直接失败
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "交易失败", oriRespCode, oriRespMsg);
					} else {//未确定的，查询确定
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", oriRespCode, oriRespMsg);
					}
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", headInfo.code, headInfo.message);
				}
			} else {//跨行
				HeadInfo headInfo = CommonParser.diffParseHead(root);
				String oriReqSerialNo = DomUtil.getTextTrim(root, "OriReqSerialNo");//原交易流水
				String oriExecType = DomUtil.getTextTrim(root, "OriExecType");//原交易响应类型
				String oriExecCode = DomUtil.getTextTrim(root, "OriExecCode");//原交易响应代码
				String oriExecMsg = DomUtil.getTextTrim(root, "OriExecMsg");//原交易响应描述
				if (payments[0].getBankBatchSeqId().equalsIgnoreCase(oriReqSerialNo) && Constants.RESPONSE_TYPE_SUCCESS.equals(headInfo.type)) {//防止返回的报文不是对应的支付报文
					if (Constants.RESPONSE_TYPE_SUCCESS.equals(oriExecType) && Constants.RESPONSE_CODE_SUCCESS.equals(oriExecCode)) {//直接成功
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "交易成功", oriExecCode, oriExecMsg);
					} else if (XmcmbcHelper.isFail(oriExecCode)) {//直接失败
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "交易失败", oriExecCode, oriExecMsg);
					} else {//未确定的，查询确定
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", oriExecCode, oriExecMsg);
					}
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", headInfo.code, headInfo.message);
				}
			}
		} catch (Exception e) {
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "代付响应报文体解密异常,查询确认", "", "");
			throw new ParseMessageException("解密发生异常:" + e);
		}
	}

	@Override
	public TransCode getTransCode() {
		return TransCode.QUERY;
	}

	@Override
	public void query(PaymentEntity[] payments) throws BaseTransException, IOException {
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String ip = config.getIp().getVal();
		int port = config.getPort().getIntVal();
		int diffPort = config.getDiffPort().getIntVal();
		int timeout = config.getTimeout().getIntVal();
		String poxy = config.getPoxy().getVal();
		String charsetName = config.getCharset().getVal();

		int interval = config.getQueryInterval().getIntVal();//查询时间间隔
		long queryTimeMs = payments[0].getSubmitPayTime().getTime() + interval * 60 * 1000;
		if (new Date().getTime() < queryTimeMs) {
			PaymentStatus.setStatus(payments, payments[0].getStatus(), interval + "分钟后发起状态查询", "", "");
			return;
		}

		StringBuilder ids = new StringBuilder();
		for (int i = 0; i < payments.length; i++) {
			if (i > 0) ids.append(",");
			ids.append(payments[i].getId());
		}
		// 查询唯一流水号
		String tranId = Sequence.genSequence();
		byte[] rsqt = pack(payments, tranId);
		logger.info("准备【查询交易状态】请求报文(paymentId:[{}], batchSeqId:{})", ids.toString(), payments[0].getBatchSeqId());
		String rspStr = null;
		if (XmcmbcHelper.isToSameBack(payments[0].getCustomerBankFullName())) {//本行
			if ("false".equals(poxy)) {
				try {
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
				}
			}
		} else {
			if ("false".equals(poxy)) {
				try {
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
				}
			}
		}
		logger.info("接收【查询交易状态】返回报文(paymentId:[{}], batchSeqId:{}): \n{}", ids.toString(), payments[0].getBatchSeqId(), rspStr);
		// 解析报文
		parse(rspStr, payments);
	}
}
