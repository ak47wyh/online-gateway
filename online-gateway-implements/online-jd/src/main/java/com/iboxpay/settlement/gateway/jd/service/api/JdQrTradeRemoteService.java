package com.iboxpay.settlement.gateway.jd.service.api;

import com.iboxpay.settlement.gateway.jd.service.model.JdNotifyVerifyReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdNotifyVerifyRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQrReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQrRespParam;

public interface JdQrTradeRemoteService {
	/**
	 * 预下单
	 * @param req
	 * @return
	 */
	JdQrRespParam doPreCreate(JdQrReqParam reqParam);	

	
	/**
	 * 验证支付宝异步通知
	 * @param req
	 * @return
	 */
	public JdNotifyVerifyRespParam doNotifyVerify(JdNotifyVerifyReqParam reqParam) ;
	

}
