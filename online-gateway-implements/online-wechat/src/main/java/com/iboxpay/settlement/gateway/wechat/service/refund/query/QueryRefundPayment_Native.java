package com.iboxpay.settlement.gateway.wechat.service.refund.query;

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
import com.iboxpay.settlement.gateway.common.trans.refund.query.AbstractRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.wechat.WechatFrontEndConfig;
import com.iboxpay.settlement.gateway.wechat.service.PaymentWechatService;
import com.iboxpay.settlement.gateway.wechat.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wechat.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;

@Service
public class QueryRefundPayment_Native extends AbstractRefundQueryPayment{
	private static Logger logger = LoggerFactory.getLogger(QueryRefundPayment_Native.class);
	private final static String TRANS_CODE_QUERY_NATIVE= "queryRefundNative";
	@Override
	public String getBankTransCode() {
		return TRANS_CODE_QUERY_NATIVE;
	}

	@Override
	public String getBankTransDesc() {
		return "微信退款查询";
	}

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
		// 获取前置机
		WechatFrontEndConfig wechatConfig = (WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String, String> map = PaymentWechatService.initCommonQueryData(wechatConfig, paymentEntity);
		
		// 提交报文信息                
        String reqContext=XmlUtils.parseXML(map);
		return reqContext;
	}

	@Override
	public void parse(String response, PaymentEntity[] payments) throws ParseMessageException {
		// 获取前置机
		WechatFrontEndConfig wechatConfig = (WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset = wechatConfig.getCharset().getVal();
		try {
			PaymentEntity paymentEntity=payments[0];
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
				String errCode = resultMap.get("err_code");
				String errCodeDes = resultMap.get("err_code_des");
				if (resultMap.containsKey("sign") && !SignUtils.checkParam(resultMap, merchantKey)) {
					logger.error("验证签名不通过!");
				} else if (resultCode.equals(WeChatContrants.RESULT_CODE_SUCCESS)) {
					/**
					 * SUCCES—退款成功 FAIL—退款失败 PROCESSING—退款处理中 NOTSURE—未确定， 需要商户
					 * CHANGE—转入代发
					 */
					Integer refund_count = Integer.valueOf(resultMap.get("refund_count"));
					String tradeState = String.valueOf(resultMap.get("refund_status" + "_" + (refund_count - 1)));
					if (tradeState.equals(WeChatContrants.REFUND_STATE_SUCCESS)) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_SUCCESS, "", "SUCCESS", "退款成功");
					} else if (tradeState.equals(WeChatContrants.REFUND_STATE_FAIL)) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", errCode, "退款失败");
					} else if (tradeState.equals(WeChatContrants.REFUND_STATE_PROCESSING)) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND, "",
								WeChatContrants.REFUND_STATE_PROCESSING, "退款处理中");
					}
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", errCode, errCodeDes);
				}
			} else {
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", returnCode, returnMsg);
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
		return ((WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getNativeRefundQueryUrl().getVal();
	}

}
