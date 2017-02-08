package com.iboxpay.settlement.gateway.wechat.service.payment;

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
import com.iboxpay.settlement.gateway.common.trans.payment.AbstractPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.wechat.WechatFrontEndConfig;
import com.iboxpay.settlement.gateway.wechat.service.PaymentWechatService;
import com.iboxpay.settlement.gateway.wechat.service.query.QueryPayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;

@Service
public class Payment_Jsapi extends AbstractPayment{
	private static Logger logger = LoggerFactory.getLogger(Payment_Jsapi.class);
	public static final String BANK_TRANS_CODE = "wechatJsapi";
	public static final String BANK_TRANS_DESC = "微信公众号支付";
	@Override
	public PaymentNavigation navigate() {
		return PaymentNavigation.create()
				.setBatchSize(1)//单笔
				.setDiffBank(true)//跨行
				.setSameBank(true)
				.setToPrivate(true)
				.setToCompany(true)//对公,对私都支持
				.setType(PaymentNavigation.Type.online);
	}

	@Override
	public String check(PaymentEntity[] payments) {
		return null;
	}

	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment_Native.class;
	}

	@Override
	public String getBankTransCode() {
		return BANK_TRANS_CODE;
	}

	@Override
	public String getBankTransDesc() {
		return BANK_TRANS_DESC;
	}

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
	    // 获取前置机
		WechatFrontEndConfig wechatConfig=(WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String,String> map =PaymentWechatService.initJsapiPayData(wechatConfig, paymentEntity);
		
		// 提交报文信息
		String reqContent = XmlUtils.parseXML(map);
		return reqContent;
	}

	@Override
	public void parse(String response, PaymentEntity[] payments) throws ParseMessageException {
		// 获取前置机
		WechatFrontEndConfig config = (WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset = config.getCharset().getVal();
		
		PaymentEntity paymentEntity=payments[0];
		Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
		// 公众号
		String appId = String.valueOf(merchantMap.get("appId"));
		// 商户交易秘钥
		String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		// 子公众号
		String subAppId = String.valueOf(merchantMap.get("subAppId"));
		// 子公众号密钥
		String subAppSecret = String.valueOf(merchantMap.get("subAppSecret"));
		try {
			byte[] xmlBytes = response.getBytes(charset);
			Map<String, String> resultMap = XmlUtils.toMap(xmlBytes, charset);
			String returnCode=resultMap.get("return_code");
			String returnMsg= resultMap.get("return_msg");
			if (returnCode.equals("SUCCESS")) {
				String resultCode=resultMap.get("result_code");
				String errCode=resultMap.get("err_code");
				String errCodeDes=resultMap.get("err_code_des");
				if(resultCode.equals("SUCCESS")){
					String prepayId = resultMap.get("prepay_id");
					// 拼装返回扩展参数
					String callbackExtProperties = PaymentWechatService.initJsApiCallbackExtProperties(appId,merchantKey, prepayId, config);
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUBMITTED, "", "SUCCESS", "提交成功",callbackExtProperties);
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errCodeDes);
				}
			}else{
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", returnCode, returnMsg);
			}

		} catch (UnsupportedEncodingException e) {
			logger.error("解析反馈报文异常：" + e.getStackTrace());
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", "0", "支付异常:"+e.getMessage());
		} catch (Exception e) {
			logger.error("解析反馈报文异常：" + e.getStackTrace());
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", "0", "支付异常:"+e.getMessage());
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
		return ((WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getNativePayUrl().getVal();
	}

}
