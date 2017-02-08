package com.iboxpay.settlement.gateway.xmcmbc.service.balance;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.BalanceEnity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.balance.IBalance;
import com.iboxpay.settlement.gateway.common.util.CurrencyUtil;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.DomUtil;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.xmcmbc.Constants;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcFrontEndConfig;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser;
import com.iboxpay.settlement.gateway.xmcmbc.service.CommonParser.HeadInfo;
/**
 * 3005	对公账号余额查询
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
@Service
public class Balance_3005 extends ConnectionAdapter implements IBalance{
	
	private final Logger logger = LoggerFactory.getLogger(Balance_3005.class);
	private static final String BANK_TRANS_CODE_VABALANCE = "3005";
	
	
	@Override
	public TransCode getTransCode() {
		return TransCode.BALANCE;
	}
	
	@Override
	public String getBankTransCode() {
		return BANK_TRANS_CODE_VABALANCE;
	}
	
	@Override
	public String getBankTransDesc() {
		return "对公账号余额查询";
	}
	
	@Override
	public void queryBalance(BalanceEnity[] balanceEntities)
			throws BaseTransException, IOException {
		String rsqt = pack(balanceEntities);
		logger.info("发送【查询余额】报文：" + rsqt);
		String rsp = "";
//		synchronized (socketHelper.getObject()) {
//			try {
//        		Socket socket = socketHelper.getSocket();
//				try {
//					OutputStream os = socket.getOutputStream();
//					// 发送请求
//					send(os, rsqt);
//					// 获取输入流
//					InputStream is = socket.getInputStream();
//					// 接收报文
//					rsp = CommonParser.read(is);
//				} catch (Throwable e) {
//					String errorMsg = e.getMessage();
//					logger.error("余额查询时候发生异常:"+errorMsg,e);
//            		boolean toException = errorMsg.indexOf(Configuration.READ_TIME_OUT) > 0;
//            		if(!toException){//对于读取超时的，不再重置Socket
//            			socketHelper.reset();
//            		}
//            		throw new ParseMessageException(e.getMessage(),e);
//				}
//			} catch (Throwable e) {//获取Socket发生异常,也即网络出现问题
//        		logger.error("", e);
//        		throw new ParseMessageException(e.getMessage(),e);
//        	}
//		}
		logger.info("接收【查询余额】返回报文：" + rsp);
		// 解析报文
		parse(rsp, balanceEntities);
	}
	
	//组装查询请求报文
	private String pack(BalanceEnity[] balanceEntities) throws PackMessageException{
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig)context.getFrontEndConfig();
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("TRAN_REQ");
		DomUtil.addChild(root, "COMPANY_ID", config.getCompanyId().getVal());//合作方id,由银行分配
		//交易时间20150918170926
		String transTime = DateTimeUtil.format(new Date(), Constants.TRANS_TIME_FORMAT);
		DomUtil.addChild(root, "TRAN_DATE", transTime.substring(0,8));//交易日期(yyyyMMdd),交易日期
		DomUtil.addChild(root, "TRAN_TIME", transTime.substring(8));//交易时间
		DomUtil.addChild(root, "TRAN_ID", Sequence.genSequence());//合作方流水号
		AccountEntity  account = context.getMainAccount();
		DomUtil.addChild(root, "ACC_NO", account.getAccNo());//查询账号
		DomUtil.addChild(root, "ACC_NAME", account.getAccName());//查询账号户名
		String xml = DomUtil.documentToString(root.getDocument(), context.getCharset());
		String send = null;//CommonPacker.pack(BANK_TRANS_CODE_VABALANCE, xml,context.getCharset());
		return send;
	}
	
	//解析
	public void parse(String respStr, BalanceEnity[] balanceEntities) 
		throws ParseMessageException{
		BalanceEnity balanceEntity = balanceEntities[0];
		Element root = DomUtil.parseXml(respStr);
        HeadInfo headInfo = CommonParser.parseHead(root);
        String rspCode =  headInfo.code;//应答码
        String rspMsg = headInfo.message;//应答描述
        if(Constants.RESPONSE_TYPE_SUCCESS.equals(headInfo.type)){//查询成功
        	String accAmount = DomUtil.getTextTrim(root, "ACC_BALANCE");//账号余额，单位为分
        	accAmount = CurrencyUtil.convertToYuan(accAmount);//转化为元
        	String avaliableAmount = DomUtil.getTextTrim(root, "AVA_BALANCE");//可用余额，单位为分
        	avaliableAmount = CurrencyUtil.convertToYuan(avaliableAmount);//转化为元
        	if("00".equals(rspCode) && !StringUtils.isBlank(accAmount) && !StringUtils.isBlank(avaliableAmount)){//返回正常的余额值
        		balanceEntity.setBalance(new BigDecimal(accAmount));
        		balanceEntity.setAvailableBalance(new BigDecimal(avaliableAmount));
        	} else {
        		throw new ParseMessageException("返回余额异常，返回状态码[" + rspCode + "]，状态信息[" + rspMsg + "]");
        	}
        } else {
    		throw new ParseMessageException("查询异常，返回状态码[" + rspCode + "]，状态信息[" + rspMsg + "]");
        }
	}
}
