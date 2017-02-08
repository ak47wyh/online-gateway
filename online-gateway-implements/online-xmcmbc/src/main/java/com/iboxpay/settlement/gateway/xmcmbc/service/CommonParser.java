package com.iboxpay.settlement.gateway.xmcmbc.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.ArrayUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.util.DomUtil;

/**
 * The class CommonParser.
 *
 * Description: 
 *
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public class CommonParser{
	private static Logger logger = LoggerFactory.getLogger(CommonParser.class);
	public static HeadInfo parseHead(Element root) throws ParseMessageException {
        String type = DomUtil.getTextTrimNotNull(root, "RESP_TYPE");
        String code = DomUtil.getTextTrim(root, "RESP_CODE");
        String message = DomUtil.getTextTrim(root, "RESP_MSG");
        return new HeadInfo(type,code, message);
    }
	
	public static HeadInfo diffParseHead(Element root) throws ParseMessageException {
        String type = DomUtil.getTextTrimNotNull(root, "ExecType");
        String code = DomUtil.getTextTrim(root, "ExecCode");
        String message = DomUtil.getTextTrim(root, "ExecMsg");
        return new HeadInfo(type,code, message);
    }
	
	public static HeadInfo diffRspHead(Element root) throws ParseMessageException {
        String type = DomUtil.getTextTrimNotNull(root, "ExecType");
        String code = DomUtil.getTextTrim(root, "ExecCode");
        String message = DomUtil.getTextTrim(root, "ExecMsg");
        return new HeadInfo(type,code, message);
    }
	
	public static class HeadInfo {

		public final String type;
        public final String code;
        public final String message;

		public HeadInfo(String type,String code, String message) {
			super();
			this.type = type;
			this.code = code;
			this.message = message;
		}
    }
	/**
	 * 长连接读取银行报文(报文头+xml报文体-变长+mac密钥)
	 * @param is
	 * @return
	 * @throws FrontEndException
	 */
	public static String read(InputStream is) throws FrontEndException {
		FrontEndConfig feConfig = TransContext.getContext().getFrontEndConfig();
        byte[] recvBuffer = new byte[8];//报文头(报文长度+服务码)固定长度21个字节
        int length = 0;
		try {
			length = is.read(recvBuffer);
			if (length < 0) {//
				logger.error("连接1...已关闭");
				return null;
			}
//			String header = new String(recvBuffer, 0, length,feConfig.getCharset().getVal());
//			//xml报文体的长度
//			int bodyLength = Integer.valueOf(header.substring(0, 8));
//			//xml报文体字节数组
//			byte[] body = new byte[bodyLength];
//			int ch = 0;
//			int i = 0;
//			while((ch=is.read())!=-1 && i<=bodyLength-1){
//				body[i] = (byte)ch;
//				i++;
//			}
//			//密钥识别码(32字节长度)
//			byte[] mac = new byte[32];//密钥长度
//			int macLength = is.read(mac);
//			if(macLength < 0){
//				logger.error("连接2...已关闭");
//				return null;
//			}
			//存放整个报文字节数组
//			byte[] buffer = new byte[recvBuffer.length+body.length];
//			System.arraycopy(recvBuffer, 0, buffer, 0, recvBuffer.length);//21固定报文头长度
//			System.arraycopy(body, 0, buffer, recvBuffer.length, body.length);//xml报文
//0003911002           <?xml version="1.0" encoding="UTF-8"?><TRAN_RESP><RESP_TYPE>S</RESP_TYPE><RESP_CODE>00</RESP_CODE><RESP_MSG>交易成功</RESP_MSG><COMPANY_ID>CS</COMPANY_ID><MCHNT_CD></MCHNT_CD><TRAN_DATE>20150319</TRAN_DATE><TRAN_TIME>154543</TRAN_TIME><TRAN_ID>2015031900010000</TRAN_ID><BANK_TRAN_DATE>20150319</BANK_TRAN_DATE><BANK_TRAN_ID>2015031900537994</BANK_TRAN_ID><RESV>预留</RESV></TRAN_RESP>C0982799B108A72583D9FDDDE2B90E8D			
			//只返回xml报文
			byte[] buffer = new byte[length];
			int respLength = is.read(buffer);
			if (respLength < 0) {//
				logger.error("连接2...已关闭");
				return null;
			}
			String resp = new String(buffer, feConfig.getCharset().getVal());
			return resp;
		} catch (IOException e) {
			throw new FrontEndException(e);
		}
	}
	/**
	 * 生成批量回盘文件名
	 * @param bankBatchSeqId   银行批次号
	 * @param isSameBank       true：同行，false：跨行
	 * @return  代付文件名    返回res_outer_yyyyMMdd_NNN.txt 或者res_yyyyMMdd_NNN
	 */
	public static String genFileName(String bankBatchSeqId,boolean isSameBank){
		StringBuilder fileName = new StringBuilder();
		//类型标识:同行 req,跨行req_outer
		if(isSameBank){
			fileName.append("res_");
		} else {
			fileName.append("res_outer_");
		}
		fileName.append(bankBatchSeqId.substring(0, 8)).append("_");
		fileName.append(bankBatchSeqId.substring(8));
		fileName.append(".txt");
		return fileName.toString();
	}
	
    //民生本行解密读取返回报文为byte[]
    public static byte[] readByte(InputStream is) throws FrontEndException {
        FrontEndConfig feConfig = TransContext.getContext().getFrontEndConfig();
        byte[] lengthBuffer = new byte[8];//报文头固定长度226个字节
        int length = 0;
        byte[] xmlBytes = null;
		try {
			length = is.read(lengthBuffer);
			if(length > 0) {
				String header = new String(lengthBuffer, 0, length,feConfig.getCharset().getVal());
				//加密后报文体的长度
				int bodyLength = Integer.valueOf(header.substring(0, 8));
				//报文体字节数组
				byte[] body = new byte[bodyLength];
				
				is.read(body);
				
				byte[] signBuffer = ArrayUtils.subarray(body, 23, 27);
				String signLenStr = new String(signBuffer, feConfig.getCharset().getVal());
				int signLen = Integer.valueOf(signLenStr);
				
				xmlBytes = ArrayUtils.subarray(body, 27 + signLen, bodyLength);
			}
			
			return xmlBytes;
		} catch (IOException e) {
			throw new FrontEndException(e);
		}
    }
    
    //民生跨行解密读取返回报文为byte[]
    public static byte[] readDiffByte(InputStream is) throws FrontEndException {
        FrontEndConfig feConfig = TransContext.getContext().getFrontEndConfig();
        byte[] lengthBuffer = new byte[8];//报文头固定长度8个字节
        int length = 0;
        byte[] xmlBytes = null;
		try {
			length = is.read(lengthBuffer);
			if(length > 0) {
				String header = new String(lengthBuffer, 0, length,feConfig.getCharset().getVal());
				//加密后报文体的长度
				int bodyLength = Integer.valueOf(header.substring(0, 8));
				//报文体字节数组
				byte[] body = new byte[bodyLength];
				
				is.read(body);
				
				byte[] signBuffer = ArrayUtils.subarray(body, 16, 20);
				String signLenStr = new String(signBuffer, feConfig.getCharset().getVal());
				int signLen = Integer.valueOf(signLenStr);
				
				xmlBytes = ArrayUtils.subarray(body, 20 + signLen, bodyLength);
			}
			
			return xmlBytes;
		} catch (IOException e) {
			throw new FrontEndException(e);
		}
    }
}
