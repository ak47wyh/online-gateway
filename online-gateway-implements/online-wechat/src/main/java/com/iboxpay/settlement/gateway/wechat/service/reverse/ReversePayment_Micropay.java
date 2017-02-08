package com.iboxpay.settlement.gateway.wechat.service.reverse;

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
import com.iboxpay.settlement.gateway.wechat.WechatFrontEndConfig;
import com.iboxpay.settlement.gateway.wechat.service.PaymentWechatService;
import com.iboxpay.settlement.gateway.wechat.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wechat.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;


/**
 * 微信刷卡冲正-威福通
 * @author liaoxiongjian
 * @date 2016-1-30 10:18
 */
@Service
public class ReversePayment_Micropay extends AbstractReversePayment{
	
	private static Logger logger = LoggerFactory.getLogger(ReversePayment_Micropay.class);
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
		WechatFrontEndConfig wechatConfig=(WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
	
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String,String> map = PaymentWechatService.initCommonQueryData(wechatConfig, paymentEntity);
       
		// 提交报文信息           
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
			
			String returnCode=resultMap.get("return_code");
			String returnMsg= resultMap.get("return_msg");
			if (returnCode.equals(WeChatContrants.RETURN_CODE_SUCCESS)) {
				String resultCode = resultMap.get("result_code");
				String errCode = resultMap.get("err_code");
				String errCodeDes = resultMap.get("err_code_des");
				if (resultMap.containsKey("sign") && !SignUtils.checkParam(resultMap, merchantKey)) {
					logger.error("验证签名不通过!");
				} else if (resultCode.equals(WeChatContrants.RESULT_CODE_SUCCESS)) {
					String outTradeNo = resultMap.get("out_trade_no");
					String totalFee = resultMap.get("total_fee");
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REVERSE, "", WeChatContrants.RESULT_CODE_SUCCESS, "冲正成功");
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REVERSE_FAIL, "", errCode, errCodeDes);
				}
			}else{
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REVERSE_FAIL, "", returnCode, returnMsg);
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
		return ((WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getMicropayReverseUrl().getVal();
	}


}