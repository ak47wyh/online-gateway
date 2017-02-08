package com.iboxpay.settlement.gateway.common.trans.callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.common.utils.OkHttpUtils;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentOuterStatus;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

public class CallBackPaymentUtils {
	private final static Logger logger = LoggerFactory.getLogger(CallBackPaymentUtils.class);
	/**
	 * 发送异步消息通知方法
	 * @param payment
	 */
	public static synchronized String doSendNotifyUrl(PaymentEntity payment) {
		Map<String,Object> param=new HashMap<String,Object>();
		param.put("appCode", payment.getAppCode());
		param.put("status", "success");
		param.put("requestSystem", "online_sys");
		param.put("batchSeqId", payment.getBatchSeqId());
		param.put("errorCode", "");
		param.put("errorMsg", "");
		
		List<Map<String,Object>> list= new ArrayList<Map<String,Object>>(); 
		Map<String,Object> dateMap=new HashMap<String,Object>();
		dateMap.put("seqId", payment.getBankSeqId());
		if(payment.getStatus()==PaymentStatus.STATUS_SUCCESS){
			dateMap.put("status", "success");
		} else if(payment.getStatus()==PaymentStatus.STATUS_FAIL){
			dateMap.put("status", "fail");
		} else if(payment.getStatus()==PaymentStatus.STATUS_WAITTING_PAY){
			dateMap.put("status", "userpaying");
		} else if(payment.getStatus()==PaymentStatus.STATUS_CLOSED){
			dateMap.put("status", "closed");
		}
		dateMap.put("statusMsg", payment.getStatusMsg());
		dateMap.put("bankStatus", payment.getBankStatus());
		dateMap.put("bankStatusMsg", payment.getBankStatusMsg());
				
		// 支付成功的回调扩展参数
		Map<String,Object> extPropertiesMap=new HashMap<String,Object>();
		PaymentOuterStatus.initCallbackExtpropeties(payment, extPropertiesMap);
		dateMap.put("extProperties", extPropertiesMap);
		
		list.add(dateMap);
		param.put("data", list);
		
		// 获取异步通知请求连接
		String result="";
		String notifyUrl = String.valueOf(payment.getExtProperty("notifyUrl"));
		try {
			if(!StringUtils.isEmpty(notifyUrl)){
				// 数据转换
				String reqContext=JsonUtil.toJson(param);
				logger.info("CallBackDelegateService.doSendNotifyUrl 异步推送数据内容："+reqContext);
				// 发送请求
				result = OkHttpUtils.httpClientJsonPostReturnAsString(notifyUrl, param, 60);
			}else{
				logger.error("notifyUrl 回调地址为空");
			}
		} catch (Exception e) {
			logger.error("异步推送异常"+e.getMessage());
		}
		return result;
	}
}
