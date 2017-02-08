package com.iboxpay.settlement.gateway.wechat.service.refund;


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
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.refund.AbstractRefundPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.wechat.WechatFrontEndConfig;
import com.iboxpay.settlement.gateway.wechat.service.PaymentWechatService;
import com.iboxpay.settlement.gateway.wechat.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wechat.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;

/**
 * 微信退款功能
 * 注意：退款功能需要https双向证书认证
 * @author liaoxiongjian
 * @date 2016-02-02 11:12
 */
@Service
public class RefundPayment_Native extends AbstractRefundPayment{
	private static Logger logger = LoggerFactory.getLogger(RefundPayment_Native.class);
	public static final String BANK_TRANS_CODE = "refundNative";
	
	@Override
	public TransCode getTransCode() {
		 return TransCode.REFUND;
	}

	@Override
	public String getBankTransCode() {
		return BANK_TRANS_CODE;
	}

	@Override
	public String getBankTransDesc() {
		  return "微信扫码退款";
	}

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
		// 获取前置机
		WechatFrontEndConfig wechatConfig = (WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String, String> map = PaymentWechatService.initRefundData(wechatConfig, paymentEntity);

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
			Map<String,String> resultMap = XmlUtils.toMap(xmlBytes, charset);
			
			String returnCode=resultMap.get("return_code");
			String returnMsg= resultMap.get("return_msg");
			if (returnCode.equals(WeChatContrants.RETURN_CODE_SUCCESS)) {
				String resultCode=resultMap.get("result_code");
				String errCode=resultMap.get("err_code");
				String errCodeDes=resultMap.get("err_code_des");
				if (!SignUtils.checkParam(resultMap, merchantKey)) {
					logger.error("验证签名不通过!");
				} else if (resultCode.equals(WeChatContrants.RESULT_CODE_SUCCESS)) {
					String outTradeNo = resultMap.get("out_trade_no");
					String totalFee = resultMap.get("total_fee");
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND, "", WeChatContrants.RESULT_CODE_SUCCESS, "退款中");
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", errCode, errCodeDes);
				}
			}else{
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", returnCode, returnMsg);
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
		return ((WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getNativeRefundUrl().getVal();
	}
}
