package com.iboxpay.settlement.gateway.jd.service.api;

import com.iboxpay.settlement.gateway.jd.service.model.JdCancleReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdCancleRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdGatewayParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQueryStatusReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQueryStatusRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdRefundReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdRefundRespParam;

public interface JdTradeRemoteService {

    /**
     * 收单状态查询
     * @param req
     * @return
     */
    public JdQueryStatusRespParam doQueryStatus(JdQueryStatusReqParam reqParam, JdGatewayParam gatewayParam);

    /**
     * 收单撤销
     * @param req
     * @return
     */
    public JdCancleRespParam doCancelTrade(JdCancleReqParam reqParam, JdGatewayParam gatewayParam);

    /**
     * 收单退款
     * @param req
     * @return
     */
    public JdRefundRespParam doRefundTrade(JdRefundReqParam reqParam, JdGatewayParam gatewayParam);

    /**
     * 退款状态查询
     * @param req
     * @return
     */
    public JdRefundRespParam doRefundQueryStatus(JdRefundReqParam reqParam, JdGatewayParam gatewayParam);

    /**
     * 退款异步通知
     * @param req
     * @return
     */
    public boolean doRefundNotifyVerify();
}
