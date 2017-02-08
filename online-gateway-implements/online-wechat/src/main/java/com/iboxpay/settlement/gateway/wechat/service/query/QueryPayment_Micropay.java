package com.iboxpay.settlement.gateway.wechat.service.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.query.AbstractQueryPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.wechat.WechatFrontEndConfig;
import com.iboxpay.settlement.gateway.wechat.service.PaymentWechatService;
import com.iboxpay.settlement.gateway.wechat.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wechat.service.reverse.ReversePayment_Micropay;
import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;


@Service
public class QueryPayment_Micropay extends AbstractQueryPayment{
	private static Logger logger = LoggerFactory.getLogger(QueryPayment_Micropay.class);
	private final static String TRANS_CODE_QUERY_MICROPAY= "queryMicropay";
	
	@Resource
	private ReversePayment_Micropay reversePaymentMicropay;
	
	@Override
	public String getBankTransCode() {
		return TRANS_CODE_QUERY_MICROPAY;
	}

	@Override
	public String getBankTransDesc() {
		return "微信刷卡支付查询";
	}

    @Override
    public void query(PaymentEntity[] payments) throws BaseTransException {
    	// 获取前置机
    	WechatFrontEndConfig config=(WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
    	
        PaymentEntity payment = payments[0];
        int queryCount= payment.getQueryTransCount();
        int queryCountCinfig = Integer.parseInt(config.getQueryTransCount().getVal());
        if(queryCount>queryCountCinfig){//大于查询配置次数调用冲正接口
        	try {
        		reversePaymentMicropay.reverse(payments);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("交易冲正异常："+e);
			}
        }else {
	        int interval = Integer.parseInt(config.getQueryInterval().getVal());
	        long queryTimeMs = payment.getTransDate().getTime() + interval * 1000;
	        if (new Date().getTime() > queryTimeMs) {
	            super.query(payments);
	        } else {
	            PaymentStatus.setStatus(payments, payment.getStatus(), interval + "秒后才可以发起状态查询", "", "");
	        }
        }
    }
	
	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
	    // 获取前置机
		WechatFrontEndConfig wechatConfig=(WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String,String> map = PaymentWechatService.initQueryDate(wechatConfig, paymentEntity);

		// 提交报文信息   
		String reqContent = XmlUtils.parseXML(map);
		return reqContent;
	}

	@Override
	public void parse(String response, PaymentEntity[] payments) throws ParseMessageException {
	    // 获取前置机
		WechatFrontEndConfig wechatConfig=(WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset =wechatConfig.getCharset().getVal();
		PaymentEntity paymentEntity=payments[0];
		try {
			byte[] xmlBytes = response.getBytes(charset);
			Map<String, String> resultMap = XmlUtils.toMap(xmlBytes, charset);
			String returnCode=resultMap.get("return_code");
			String returnMsg= resultMap.get("return_msg");
			if (returnCode.equals(WeChatContrants.RETURN_CODE_SUCCESS)) {
				String resultCode=resultMap.get("result_code");
				String errCode=resultMap.get("err_code");
				String errCodeDes=resultMap.get("err_code_des");
				if(resultCode.equals(WeChatContrants.RESULT_CODE_SUCCESS)){
					String outTradeNo=resultMap.get("out_trade_no");
					String callbackExtProperties = PaymentWechatService.initCallbackExtProperties(resultMap,paymentEntity);
					/**
					 * SUCCESS—支付成功
					 * REFUND—转入退款
					 * NOTPAY—未支付
					 * CLOSED—已关闭
					 * REVOKED—已撤销(刷卡支付)
					 * USERPAYING--用户支付中
					 * PAYERROR--支付失败(其他原因，如银行返回失败)
					 */
					String tradeState = String.valueOf(resultMap.get("trade_state"));
					if (tradeState.equals(WeChatContrants.TRADE_STATE_SUCCESS)) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "", "SUCCESS", "支付成功",callbackExtProperties);
					} else if (tradeState.equals(WeChatContrants.TRADE_STATE_CLOSED)) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_CLOSED, "", errCode, errCodeDes,callbackExtProperties);
					} else if(tradeState.equals(WeChatContrants.TRADE_STATE_REVOK)){
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_CANCEL, "", errCode, errCodeDes,callbackExtProperties);
					} else if(tradeState.equals(WeChatContrants.TRADE_STATE_REFUND)){
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND, "", errCode, errCodeDes,callbackExtProperties);
					} else if(tradeState.equals(WeChatContrants.TRADE_STATE_USERPAYING)){
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_WAITTING_PAY, "", errCode, errCodeDes,callbackExtProperties);
					} else if(tradeState.equals(WeChatContrants.TRADE_STATE_PAYERROR)){
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errCodeDes,callbackExtProperties);
					} else if(tradeState.equals(WeChatContrants.TRADE_STATE_NOTPAY)){
						String tradeStateDesc=resultMap.get("trade_state_desc");
						logger.info("订单号："+outTradeNo+"=>错误原因："+tradeStateDesc);
					}		
				}else{
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errCodeDes);
				}
			}else{
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", returnCode, returnMsg);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("解析反馈报文异常："+e.getStackTrace());
		} catch (Exception e) {
			logger.error("解析反馈报文异常："+e.getStackTrace());
		}
	}


	
	
	@Override
	public Map<String, String> getHeaderMap() {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "text/xml; charset=utf-8");
		return headerMap;
	}

	@Override
	protected String getUri() {
		return ((WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getMicropayQueryUrl().getVal();
	}
	
	


}
