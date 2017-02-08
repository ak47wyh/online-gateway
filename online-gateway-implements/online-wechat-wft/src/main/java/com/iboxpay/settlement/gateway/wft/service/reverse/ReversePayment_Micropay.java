package com.iboxpay.settlement.gateway.wft.service.reverse;

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
import com.iboxpay.settlement.gateway.common.trans.reverse.AbstractReversePayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.wft.WftFrontEndConfig;
import com.iboxpay.settlement.gateway.wft.service.PaymentWeChatService;
import com.iboxpay.settlement.gateway.wft.service.query.QueryPayment_Micropay;
import com.iboxpay.settlement.gateway.wft.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wft.service.utils.XmlUtils;

/**
 * 微信刷卡冲正-威富通
 * @author liaoxiongjian
 * @date 2016-1-30 10:18
 */
@Service
public class ReversePayment_Micropay extends AbstractReversePayment{
	
	private static Logger logger = LoggerFactory.getLogger(QueryPayment_Micropay.class);
	private final static String TRANS_CODE_REVERSE_MICROPAY= "reverseMicropay";
	@Override
	public String getBankTransCode() {
		return TRANS_CODE_REVERSE_MICROPAY;
	}

	@Override
	public String getBankTransDesc() {
		return "微信刷卡冲正";
	}

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
	    // 获取前置机
		WftFrontEndConfig config=(WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();

		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String,String> map = PaymentWeChatService.initReverseData(config, paymentEntity);
		    
		String reqContent = XmlUtils.parseXML(map);
		return reqContent;
	}

	@Override
	public void parse(String response, PaymentEntity[] payments) throws ParseMessageException {
	    // 获取前置机
		WftFrontEndConfig config=(WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset = config.getCharset().getVal();
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
				if ("0".equals(resultMap.get("status")) && "0".equals(resultMap.get("result_code"))) {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REVERSE, "", "0", "冲正成功");					
				} else {
					String errCode = (String) resultMap.get("err_code");
					String errMsg = (String) resultMap.get("err_msg");
					String message = resultMap.get("message");
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REVERSE_FAIL, "", errCode, errMsg);
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("解析冲正报文异常："+e.getStackTrace());
		} catch (Exception e) {
			logger.error("解析冲正报文异常："+e.getStackTrace());
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
