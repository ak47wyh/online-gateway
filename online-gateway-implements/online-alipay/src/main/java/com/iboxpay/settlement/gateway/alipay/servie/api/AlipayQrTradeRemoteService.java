package com.iboxpay.settlement.gateway.alipay.servie.api;

import com.iboxpay.settlement.gateway.alipay.servie.model.AlipayGatewayParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.CancelTradeReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.CancelTradeRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.NotifyVerifyReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.PreCreateReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.PreCreateRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.QueryStatusReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.QueryStatusRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.RefundTradeReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.RefundTradeRespParam;

/**
 * 支付宝收单远程接口
 * @author Jim Yang (oraclebone@gmail.com)
 *
 */
public interface AlipayQrTradeRemoteService {

	/**
	 * 预下单
	 * @param req
	 * @return
	 */
	PreCreateRespParam doPreCreate(PreCreateReqParam req,AlipayGatewayParam gatewayParam);
	
	/**
	 * 收单状态查询
	 * @param req
	 * @return
	 */
	QueryStatusRespParam doQueryStatus(QueryStatusReqParam req,AlipayGatewayParam gatewayParam);
	
	/**
	 * 收单撤销
	 * @param req
	 * @return
	 */
	CancelTradeRespParam doCancelTrade(CancelTradeReqParam req,AlipayGatewayParam gatewayParam);

	/**
	 * 收单退款
	 * @param req
	 * @return
	 */
	RefundTradeRespParam doRefundTrade(RefundTradeReqParam req,AlipayGatewayParam gatewayParam);
	
	/**
	 * 验证支付宝异步通知
	 * @param req
	 * @return
	 */
	boolean doNotifyVerify(NotifyVerifyReqParam req,AlipayGatewayParam gatewayParam);
	
}
