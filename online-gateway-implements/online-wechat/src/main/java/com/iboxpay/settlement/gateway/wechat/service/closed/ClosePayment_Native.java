package com.iboxpay.settlement.gateway.wechat.service.closed;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.callback.CallBackPaymentUtils;
import com.iboxpay.settlement.gateway.common.trans.close.AbstractClosePayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.wechat.WechatFrontEndConfig;
import com.iboxpay.settlement.gateway.wechat.service.PaymentWechatService;
import com.iboxpay.settlement.gateway.wechat.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wechat.service.query.QueryPayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;

@Service
public class ClosePayment_Native extends AbstractClosePayment{
	private static Logger logger = LoggerFactory.getLogger(QueryPayment_Native.class);
	
	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
	    // 获取前置机
		WechatFrontEndConfig wechatConfig=(WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String,String> map = PaymentWechatService.initCommonQueryData(wechatConfig, paymentEntity);
	        
		String reqContent = XmlUtils.parseXML(map);
		return reqContent;
	}

	@Override
	public void parse(String response, PaymentEntity[] payments) throws ParseMessageException {
	    // 获取前置机
		WechatFrontEndConfig wechatConfig=(WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset = wechatConfig.getCharset().getVal();
		PaymentEntity paymentEntity=payments[0];
		try {
			String merchantExtProperties=paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExtProperties);
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			
			byte[] xmlBytes = response.getBytes(charset);
			Map<String, String> resultMap = XmlUtils.toMap(xmlBytes, charset);
			String returnCode = resultMap.get("return_code");
			String returnMsg = resultMap.get("return_msg");
			if (returnCode.equals(WeChatContrants.RETURN_CODE_SUCCESS)) {
				String resultCode = resultMap.get("result_code");
				if (resultMap.containsKey("sign") && !SignUtils.checkParam(resultMap, merchantKey)) {
					logger.error("验证签名不通过!");
				} else if (resultCode.equals(WeChatContrants.RESULT_CODE_SUCCESS)) {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_CLOSED, "", WeChatContrants.RESULT_CODE_SUCCESS, "关闭成功");
					
					//发送异步通知请求【对接网关的系统】
					String result=CallBackPaymentUtils.doSendNotifyUrl(payments[0]);
					logger.info("QueryPayment_Native.query 关闭订单异步推送返回："+result);
				} 
			} else {
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_CLOSED_FAIL, "", returnCode, returnMsg);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("解析反馈报文异常：" + e.getStackTrace());
		} catch (Exception e) {
			logger.error("解析反馈报文异常：" + e.getStackTrace());
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
		return ((WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getCloseUrl().getVal();
	}

}
