package com.iboxpay.settlement.gateway.wft.service.close;

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
import com.iboxpay.settlement.gateway.common.trans.close.AbstractClosePayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.wft.WftFrontEndConfig;
import com.iboxpay.settlement.gateway.wft.service.PaymentWeChatService;
import com.iboxpay.settlement.gateway.wft.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wft.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wft.service.utils.XmlUtils;

@Service
public class ClosePayment_Native extends AbstractClosePayment{
	private static Logger logger = LoggerFactory.getLogger(ClosePayment_Native.class);

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
	    // 获取前置机
		WftFrontEndConfig config=(WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String,String> map = PaymentWeChatService.initCloseDate(config, paymentEntity);
		
		String reqContent = XmlUtils.parseXML(map);
		return reqContent;
	}

	
	@Override
	public void parse(String respStr, PaymentEntity[] payments) throws ParseMessageException {
		// 获取前置机
		WftFrontEndConfig config=(WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset =config.getCharset().getVal();
		try {
			PaymentEntity paymentEntity=payments[0];
			String merchantExt= paymentEntity.getMerchantExtProperties();
			Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			
			byte[] xmlBytes = respStr.getBytes(charset);
			Map<String, String> resultMap = XmlUtils.toMap(xmlBytes, charset);
			if (resultMap.containsKey("sign") && !SignUtils.checkParam(resultMap, merchantKey)) {
				logger.error("验证签名不通过!");
			} else {
				if (WeChatContrants.STATUS_SUCCESS.equals(resultMap.get("status")) && WeChatContrants.RESULT_CODE_SUCCESS.equals(resultMap.get("result_code"))) {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_CLOSED, "", "SUCCESS", "关闭成功");
				} else {
					String errCode = (String) resultMap.get("err_code");
					String errMsg = (String) resultMap.get("err_msg");
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_CLOSED_FAIL, "", errCode, errMsg);
				}
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
		return ((WftFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getUri().getVal();
	}

}
