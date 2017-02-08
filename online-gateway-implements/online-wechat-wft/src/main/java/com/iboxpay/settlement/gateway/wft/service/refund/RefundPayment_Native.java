package com.iboxpay.settlement.gateway.wft.service.refund;


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
import com.iboxpay.settlement.gateway.wft.WftFrontEndConfig;
import com.iboxpay.settlement.gateway.wft.service.PaymentWeChatService;
import com.iboxpay.settlement.gateway.wft.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wft.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wft.service.utils.XmlUtils;


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
		WftFrontEndConfig config = (WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();
				
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String, String> map=PaymentWeChatService.initRefundData(config, paymentEntity);
		
		// map数据转换转化成xml格式的字符串
        String reqContext=XmlUtils.parseXML(map);
		return reqContext;
	}

	@Override
	public void parse(String response, PaymentEntity[] payments) throws ParseMessageException {
		// 获取前置机
		WftFrontEndConfig config = (WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset=config.getCharset().getVal();
		try {
			PaymentEntity paymentEntity=payments[0];
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			
			byte[] xmlBytes = response.getBytes(charset);
			Map<String,String> resultMap = XmlUtils.toMap(xmlBytes, charset);

			if (resultMap.containsKey("sign")) {
				if (!SignUtils.checkParam(resultMap, merchantKey)) {
					logger.error("验证签名不通过!");
				} else {
					if (WeChatContrants.STATUS_SUCCESS.equals(resultMap.get("status")) && WeChatContrants.RESULT_CODE_SUCCESS.equals(resultMap.get("result_code"))) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND, "", WeChatContrants.RESULT_CODE_SUCCESS, "提交成功");
					} else {
						String errCode = (String) resultMap.get("err_code");
						String errMsg = (String) resultMap.get("err_msg");
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errMsg);
					}
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
