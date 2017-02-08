package com.iboxpay.settlement.gateway.jd.service.api;

import com.iboxpay.settlement.gateway.jd.service.model.JdMicropayReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdMicropayRespParam;

public interface JdMicropayRemoteService {
	
	/**
	 * -提交刷卡支付API
	 * 
	 * @param req
	 * @return
	 */
	JdMicropayRespParam doMicroPay(JdMicropayReqParam param);
	

}
