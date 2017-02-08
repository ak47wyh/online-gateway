package com.iboxpay.settlement.gateway.wft.service.query;

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
import com.iboxpay.settlement.gateway.wft.WftFrontEndConfig;
import com.iboxpay.settlement.gateway.wft.service.PaymentWeChatService;
import com.iboxpay.settlement.gateway.wft.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wft.service.reverse.ReversePayment_Micropay;
import com.iboxpay.settlement.gateway.wft.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wft.service.utils.XmlUtils;

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
    	WftFrontEndConfig config=(WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();
    	
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
	        long queryTimeMs = payment.getSubmitPayTime().getTime() + interval * 1000;
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
		WftFrontEndConfig config=(WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String,String> map=PaymentWeChatService.initQueryDate(config, paymentEntity);

		String reqContent = XmlUtils.parseXML(map);
		return reqContent;
	}

	@Override
	public void parse(String response, PaymentEntity[] payments) throws ParseMessageException {
		 // 获取前置机
		WftFrontEndConfig config=(WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset=config.getCharset().getVal();
		try {
			PaymentEntity paymentEntity=payments[0];
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			
			byte[] xmlBytes = response.getBytes(charset);
			Map<String, String> resultMap = XmlUtils.toMap(xmlBytes, charset);
			if (resultMap.containsKey("sign") && !SignUtils.checkParam(resultMap, merchantKey)) {
				logger.error("验证签名不通过!");
			} else {
				if (WeChatContrants.STATUS_SUCCESS.equals(resultMap.get("status")) && WeChatContrants.RESULT_CODE_SUCCESS.equals(resultMap.get("result_code"))) {
					/**
					 * SUCCESS—支付成功
					 * REFUND—转入退款
					 * NOTPAY—未支付
					 * CLOSED—已关闭
					 * REVERSE—已冲正
					 * REVOK—已撤销
					 */
					String errCode = (String) resultMap.get("err_code");
					String errMsg = (String) resultMap.get("err_msg");
					String tradeState = String.valueOf(resultMap.get("trade_state"));
					String callbackExtProperties = PaymentWeChatService.initCallbackExtProperties(resultMap,paymentEntity);
					if (tradeState.equals(WeChatContrants.TRADE_STATE_SUCCESS)) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "", tradeState, "支付成功",callbackExtProperties);
					} else if (tradeState.equals(WeChatContrants.TRADE_STATE_CLOSED)) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_CLOSED, "", errCode, errMsg,callbackExtProperties);
					} else if(tradeState.equals(WeChatContrants.TRADE_STATE_REVOK)){
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_CANCEL, "", errCode, errMsg,callbackExtProperties);
					} else if(tradeState.equals(WeChatContrants.TRADE_STATE_REFUND)){
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND, "", errCode, errMsg,callbackExtProperties);
					} else if(tradeState.equals(WeChatContrants.TRADE_STATE_PAYERROR)){
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errMsg,callbackExtProperties);
					} 						
				} else {
					String errCode = (String) resultMap.get("err_code");
					String errMsg = (String) resultMap.get("err_msg");
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errMsg);
				}
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
		return ((WftFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getUri().getVal();
	}

}
