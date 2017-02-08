package com.iboxpay.settlement.gateway.xmcmbc.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.util.CurrencyUtil;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.DomUtil;
import com.iboxpay.settlement.gateway.xmcmbc.Constants;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcFrontEndConfig;

/**
 * 封装请求报文
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
public class CommonPacker {
	
	private static final String SEPARATOR_CHAR = "|";//字段之间的分隔符
	private static final String SEPARATOR_WRAP = "\r\n";//行之间分隔符
	private static final String FILE_END_CHAR = "########";//文件完整结束符
	
	/**
	 * 同行请求公共报文头
	 * @return
	 */
	public static Element packHeader() {
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig)context.getFrontEndConfig();
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("TRAN_REQ");
		DomUtil.addChild(root, "VERSION", "1.0");//版本号
//		DomUtil.addChild(root, "COMPANY_ID", config.getCompanyId().getVal());//合作方id,由银行分配
		DomUtil.addChild(root, "MCHNT_CD", config.getMchntCd().getVal());//商户编号
		DomUtil.addChild(root, "MCHNT_NAME", "");//商户名称
		//交易时间20150918170926
		String transTime = DateTimeUtil.format(new Date(), Constants.TRANS_TIME_FORMAT);
		DomUtil.addChild(root, "TRAN_DATE", transTime.substring(0,8));//交易日期(yyyyMMdd),交易日期
		DomUtil.addChild(root, "TRAN_TIME", transTime.substring(8));//交易时间
		return root;
	}
	
	/**
	 * 跨行请求公共报文头
	 * @return
	 */
	public static Element diffPackHeader() {
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig)context.getFrontEndConfig();
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Req");
//		DomUtil.addChild(root, "COMPANY_ID", config.getCompanyId().getVal());//合作方id,由银行分配
		DomUtil.addChild(root, "Version", "1.0");//目前版本号为“1.0”
		//交易时间20150918170926
		String transTime = DateTimeUtil.format(new Date(), Constants.TRANS_TIME_FORMAT);
		DomUtil.addChild(root, "TransDate", transTime.substring(0,8));//交易日期(yyyyMMdd),交易日期
		DomUtil.addChild(root, "TransTime", transTime.substring(8));//交易时间
		return root;
	}
	
	/**
	 * 同行请求公共报文头
	 * @return
	 */
	public static Element queryPackHeader() {
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig)context.getFrontEndConfig();
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("TRAN_REQ");
		DomUtil.addChild(root, "VERSION", "1.0");//版本号
//		DomUtil.addChild(root, "COMPANY_ID", config.getCompanyId().getVal());//合作方id,由银行分配
		DomUtil.addChild(root, "MCHNT_CD", config.getMchntCd().getVal());//商户编号
//		DomUtil.addChild(root, "MCHNT_NAME", "");//商户名称
		//交易时间20150918170926
		String transTime = DateTimeUtil.format(new Date(), Constants.TRANS_TIME_FORMAT);
		DomUtil.addChild(root, "TRAN_DATE", transTime.substring(0,8));//交易日期(yyyyMMdd),交易日期
		DomUtil.addChild(root, "TRAN_TIME", transTime.substring(8));//交易时间
		return root;
	}
	
	/**
	 * 跨行请求公共报文头
	 * @return
	 */
	public static Element diffQueryPackHeader() {
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig)context.getFrontEndConfig();
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("TRAN_REQ");
		DomUtil.addChild(root, "Version", "1.0");//版本号
//		DomUtil.addChild(root, "COMPANY_ID", config.getCompanyId().getVal());//合作方id,由银行分配
//		DomUtil.addChild(root, "MCHNT_CD", config.getMchntCd().getVal());//商户编号
//		DomUtil.addChild(root, "MCHNT_NAME", "");//商户名称
		//交易时间20150918170926
		String transTime = DateTimeUtil.format(new Date(), Constants.TRANS_TIME_FORMAT);
		DomUtil.addChild(root, "TransDate", transTime.substring(0,8));//交易日期(yyyyMMdd),交易日期
		DomUtil.addChild(root, "TransTime", transTime.substring(8));//交易时间
		return root;
	}
	
	/**
	 * 得到同行批量txt代付文件内容(包括文件头、文件体、文件尾)
	 * P|2|10
	 * A110150506283891|50000000000000244022|BP006213|5|批付摘要|批付备注
	 * A110150506283892|50000000000000244023|BP006214|5|批付摘要|批付备注
	 * ########
	 * @param payments   支付记录
	 * @param isSameBank true同行，false跨行
	 * @return           支付组装后的整个String报文
	 * @throws PackMessageException
	 */
	public static String genFileContent(PaymentEntity[] payments,boolean isSameBank) throws PackMessageException{
		StringBuilder file = new StringBuilder();
		//组装头部
		if(isSameBank){//P-表示行内
			file.append("P");
		} else {//PO--跨行
			file.append("PO");
		}
		file.append(SEPARATOR_CHAR);
		int totalCount = payments.length;//总笔数
		file.append(totalCount).append(SEPARATOR_CHAR);
		BigDecimal amount = new BigDecimal("0.0");
		for (PaymentEntity paymentEntity : payments) {
			amount = amount.add(paymentEntity.getAmount());
		}
		//金额，单位为分
		String totalAmount = CurrencyUtil.convertToPoint(amount, 0);
		file.append(totalAmount).append(SEPARATOR_WRAP);
		//组装报文体
		StringBuilder body = new StringBuilder();
		for (PaymentEntity payment : payments) {
			//同行文件体:第三方流水号|帐号|户名|金额|摘要|备注
			//跨行文件体:文第三方流水号|帐号|户名|支付行号|开户行名称|金额|摘要|备注
			body.append(payment.getBankSeqId()).append(SEPARATOR_CHAR)
				.append(payment.getCustomerAccNo()).append(SEPARATOR_CHAR)
				.append(payment.getCustomerAccName()).append(SEPARATOR_CHAR);
			if(!isSameBank){//跨行
				body.append(payment.getCustomerCnaps()).append(SEPARATOR_CHAR);
				body.append(payment.getCustomerBankBranchName()).append(SEPARATOR_CHAR);
			}
			String perAmount = CurrencyUtil.convertToPoint(payment.getAmount(), 0);
			body.append(perAmount).append(SEPARATOR_CHAR)
				.append("银联代付摘要").append(SEPARATOR_CHAR)
				.append("银联代付备注").append(SEPARATOR_WRAP);
		}
		file.append(body);
		//文件尾部，文件以“########”结尾，以确保文件完整性，发现文件无结尾符，标示文件不完整，不接收报盘
		file.append(FILE_END_CHAR);
		return file.toString();
	}
	/**
	 * 生成批量代付文件名
	 * @param bankBatchSeqId   银行批次号
	 * @param isSameBank       true：同行，false：跨行
	 * @return  代付文件名    返回req_outer_yyyyMMdd_NNN.txt 或者req_yyyyMMdd_NNN
	 */
	public static String genFileName(String bankBatchSeqId,boolean isSameBank){
		StringBuilder fileName = new StringBuilder();
		//类型标识:同行 req,跨行req_outer
		if(isSameBank){
			fileName.append("req_");
		} else {
			fileName.append("req_outer_");
		}
		fileName.append(bankBatchSeqId.substring(0, 8)).append("_");
		fileName.append(bankBatchSeqId.substring(8));
		fileName.append(".txt");
		return fileName.toString();
	}
	
	public static void main(String[] args) {
		String str= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TRAN_REQ><COMPANY_ID>CS</COMPANY_ID><MCHNT_CD></MCHNT_CD><TRAN_DATE>20150319</TRAN_DATE><TRAN_TIME>154543</TRAN_TIME><TRAN_ID>2015031900010000</TRAN_ID><CURRENCY>RMB</CURRENCY><ACC_NO>6226222980014414</ACC_NO><ACC_NAME>林章春</ACC_NAME><BANK_TYPE></BANK_TYPE><BANK_NAME></BANK_NAME><TRANS_AMT>5</TRANS_AMT><REMARK>备注</REMARK><RESV>预留</RESV></TRAN_REQ>";
		System.out.println(str.length());
		try {
			System.out.println(str.getBytes("UTF-8").length);
			String s1 = "0003911002           ";
			System.out.println(s1.length());
			System.out.println(s1.getBytes("UTF-8").length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
