package com.iboxpay.settlement.gateway.xmcmbc.service.query;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Resource;

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
import com.iboxpay.settlement.gateway.xmcmbc.Configuration;
import com.iboxpay.settlement.gateway.xmcmbc.Constants;
import com.iboxpay.settlement.gateway.xmcmbc.SocketHelper;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcHelper;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonPacker;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser.HeadInfo;

/**
 * 实时代付-结果查询
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
@Service
public class QueryPayment_3002 extends ConnectionAdapter implements IQueryPayment{
	
	private static Logger logger = LoggerFactory.getLogger(QueryPayment_3002.class);
	private static final String BANK_QUERY_CODE = "3009";
	
	@Resource
	private SocketHelper socketHelper;
	
	@Override
	public String getBankTransCode() {
		return BANK_QUERY_CODE;
	}

	@Override
	public String getBankTransDesc() {
		return "账务交易结果查询";
	}
	
	public String pack(PaymentEntity[] payments) throws PackMessageException {
		//0003033002           <?xml version="1.0" encoding="UTF-8"?><TRAN_REQ><COMPANY_ID>CS</COMPANY_ID><MCHNT_CD></MCHNT_CD><TRAN_DATE>20150319</TRAN_DATE><TRAN_TIME>154653</TRAN_TIME><TRAN_ID>2015031900010000</TRAN_ID><ORI_TRAN_DATE>20150319</ORI_TRAN_DATE><ORI_TRAN_ID>2015031900010000</ORI_TRAN_ID><RESV>预留</RESV></TRAN_REQ>3AAE5C56801CF1664405904B932228C6
		PaymentEntity payment = payments[0];
		TransContext context = TransContext.getContext();
		String send = null;
//		if(XmcmbcHelper.isToSameBack(payment.getCustomerBankBranchName()) || payment.isToSameBack()) {//本行
//			Element root = CommonPacker.queryPackHeader();
//			DomUtil.addChild(root, "SerialNo", Sequence.genSequence());//查询交易流水,唯一
//			//原来交易日期
//			String oldTransDate = DateTimeUtil.format(payment.getTransDate(), Constants.TRANS_DATE_FORMAT);
//			DomUtil.addChild(root, "OriTransDate", oldTransDate);
//			DomUtil.addChild(root, "OriReqSerialNo", payment.getBankBatchSeqId());//原来交易流水
//			DomUtil.addChild(root, "Resv", "");//备用域
//			String xml = DomUtil.documentToString(root.getDocument(), context.getCharset());
//			send = CommonPacker.pack(BANK_QUERY_CODE,xml,context.getCharset());
//		}
//		else {
//			Element root = CommonPacker.difPackHeader();
//			DomUtil.addChild(root, "TRAN_ID", Sequence.genSequence());//查询交易流水,唯一
//			//原来交易日期
//			String oldTransDate = DateTimeUtil.format(payment.getTransDate(), Constants.TRANS_DATE_FORMAT);
//			DomUtil.addChild(root, "ORI_TRAN_DATE", oldTransDate);
//			DomUtil.addChild(root, "ORI_TRAN_ID", payment.getBankBatchSeqId());//原来交易流水
//			DomUtil.addChild(root, "RESV", "");//备用域
//			String xml = DomUtil.documentToString(root.getDocument(), context.getCharset());
//			send = CommonPacker.difPack(BANK_QUERY_CODE,xml,context.getCharset());
//		}
		
		Element root = CommonPacker.queryPackHeader();
		DomUtil.addChild(root, "TRAN_ID", Sequence.genSequence());//查询交易流水,唯一
		//原来交易日期
		String oldTransDate = DateTimeUtil.format(payment.getTransDate(), Constants.TRANS_DATE_FORMAT);
		DomUtil.addChild(root, "ORI_TRAN_DATE", oldTransDate);
		DomUtil.addChild(root, "ORI_TRAN_ID", payment.getBankBatchSeqId());//原来交易流水
		DomUtil.addChild(root, "RESV", "");//备用域
		String xml = DomUtil.documentToString(root.getDocument(), context.getCharset());
		send = CommonPacker.pack(BANK_QUERY_CODE,xml,context.getCharset());
		
		return send;
	}

	public void parse(String respStr, PaymentEntity[] payments)
			throws ParseMessageException {
		//<?xml version="1.0" encoding="UTF-8"?><TRAN_RESP><RESP_TYPE>S</RESP_TYPE><RESP_CODE>00</RESP_CODE><RESP_MSG>交易成功</RESP_MSG><COMPANY_ID>CS</COMPANY_ID><MCHNT_CD></MCHNT_CD><TRAN_DATE>20150319</TRAN_DATE><TRAN_TIME>154653</TRAN_TIME><TRAN_ID>2015031900010000</TRAN_ID><ORI_TRAN_DATE>20150319</ORI_TRAN_DATE><ORI_TRAN_ID>2015031900010000</ORI_TRAN_ID><ORI_BANK_TRAN_DATE>20150319</ORI_BANK_TRAN_DATE><ORI_BANK_TRAN_ID>2015031900537994</ORI_BANK_TRAN_ID><ORI_RESP_TYPE>S</ORI_RESP_TYPE><ORI_RESP_CODE>00</ORI_RESP_CODE><ORI_RESP_MSG>交易成功</ORI_RESP_MSG><RESV>预留</RESV></TRAN_RESP>
		Element root = DomUtil.parseXml(respStr);
		if(XmcmbcHelper.isToSameBack(payments[0].getCustomerBankBranchName()) || payments[0].isToSameBack()) {//本行
	        HeadInfo headInfo = CommonParser.parseHead(root);
	        String oriTranId = DomUtil.getTextTrim(root, "ORI_TRAN_ID");//原交易流水
	        if(oriTranId.equalsIgnoreCase(payments[0].getBankBatchSeqId())){
	        	if(Constants.RESPONSE_TYPE_SUCCESS.equals(headInfo.type)){//成功
	        		PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "交易成功", headInfo.code, headInfo.message);
	        	} else if(Constants.RESPONSE_TYPE_ERROR.equals(headInfo.type)){//失败
	        		PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "交易失败", headInfo.code, headInfo.message);
	        	} else {//未确定的，继续查询确定
	        		PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", headInfo.code, headInfo.message);
	        	}
	        } else {
	        	PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", "", "");
	        }
		}
		else {
			HeadInfo headInfo = CommonParser.difParseHead(root);
	        String serialNo = DomUtil.getTextTrim(root, "OriReqSerialNo");//原交易流水
	        if(serialNo.equalsIgnoreCase(payments[0].getBankBatchSeqId())){
	        	if(Constants.RESPONSE_TYPE_SUCCESS.equals(headInfo.type)){//成功
	        		PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "交易成功", headInfo.code, headInfo.message);
	        	} else if(Constants.RESPONSE_TYPE_ERROR.equals(headInfo.type)){//失败
	        		PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "交易失败", headInfo.code, headInfo.message);
	        	} else {//未确定的，继续查询确定
	        		PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", headInfo.code, headInfo.message);
	        	}
	        } else {
	        	PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "未确定", "", "");
	        }
		}
	}

	@Override
	public TransCode getTransCode() {
		return TransCode.QUERY;
	}

	@Override
	public void query(PaymentEntity[] payments) throws BaseTransException,
			IOException {
		StringBuilder ids = new StringBuilder();
        for (int i = 0; i < payments.length; i++) {
            if (i > 0) ids.append(",");
            ids.append(payments[i].getId());
        }
        String rsqt = pack(payments);
        logger.info("准备【查询交易状态】请求报文(paymentId:[{}], batchSeqId:{}): \n{}", ids.toString(), payments[0].getBatchSeqId(), rsqt);
        String rsp = "";
//        if(XmcmbcHelper.isToSameBack(payments[0].getCustomerBankBranchName()) || payments[0].isToSameBack()) {//本行
//        	try {
//                openConnection();
//                OutputStream os = getOutputStream();
//                try {
//                    //发送请求
//                    send(os, rsqt);
//                    handleAfterWrite(os);
//                    // 获取输入流
//                    InputStream is = getInputStream();
//                    // 接收报文
//                    rsp = read(is);
//                    
//                    handleAfterRead(is);
//                    // 解析报文
//                    //parse(rsp, payments);
//                } catch (Throwable e) {
//                    logger.error("", e);
//                    PaymentStatus.processExceptionWhenPay(e, payments);
//                }
//            } catch (Throwable e) {
//                logger.error("", e);
//                PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", "", "");
//            } finally {
//                closeConnection();
//            }
//        }
//        else {
        	synchronized (socketHelper.getObject()) {
	        	try {
	        		OutputStream os = socketHelper.getSocket().getOutputStream();
	        		// 发送请求
	        		send(os, rsqt);
	        		// 获取输入流
	        		InputStream is = socketHelper.getSocket().getInputStream();
	        		// 接收报文
	        		rsp = CommonParser.read(is);
	        	} catch(Throwable e) {
	        		String errorMsg = e.getMessage();
	        		logger.error("支付结果查询时候发生异常:"+errorMsg,e);
	        		boolean toException = errorMsg.indexOf(Configuration.READ_TIME_OUT) > 0;
	        		if(!toException){//对于读取超时的，不再重置Socket
	        			socketHelper.reset();
	        		}
	        	}
        	}
//        }
        logger.info("接收【查询交易状态】返回报文(paymentId:[{}], batchSeqId:{}): \n{}", ids.toString(), payments[0].getBatchSeqId(), rsp);
        // 解析报文
        parse(rsp, payments);
	}
}
