package com.iboxpay.settlement.gateway.jd.service.refund.query;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.jd.JdpayFrontEndConfig;
import com.iboxpay.settlement.gateway.jd.service.api.JdTradeRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdGatewayParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdRefundReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdRefundRespParam;

@Service
public class QueryRefundPayment_JdTrade implements IRefundQueryPayment {

    private static Logger logger = LoggerFactory.getLogger(QueryRefundPayment_JdTrade.class);
    private final static String TRANS_CODE_QUERY_REFUND = "queryRefund";

    private final static String TRANS_CODE_QUERY_REFUND_DESC = "京东退款查询";

    @Resource
    private JdTradeRemoteService jdTradeRemoteService;

    @Override
    public TransCode getTransCode() {
        return TransCode.REFUNDQUERY;
    }

    @Override
    public String getBankTransCode() {
        return TRANS_CODE_QUERY_REFUND;
    }

    @Override
    public String getBankTransDesc() {
        return TRANS_CODE_QUERY_REFUND_DESC;
    }

    @Override
    public void query(PaymentEntity[] payments) throws BaseTransException, IOException {
        PaymentEntity paymentEntity = payments[0];
        JdpayFrontEndConfig feConfig = (JdpayFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        String merchantExt = paymentEntity.getMerchantExtProperties();
        Map<String, Object> merchantMap = JsonUtil.parseJSON2Map(merchantExt);
        // 交易商户号
        String merchantNo = String.valueOf(merchantMap.get("payMerchantNo"));
        // 子商户号
        String merchantSubNo = String.valueOf(merchantMap.get("payMerchantSubNo"));
        // 交易秘钥
        String merchantKey = String.valueOf(merchantMap.get("payMerchantKey"));
        String refundNo = (String) paymentEntity.getCallbackExtProperties("outRefundNo");

        JdRefundReqParam reqParam = new JdRefundReqParam();
        reqParam.setRefundNo(refundNo);
        JdGatewayParam gatewayParam = new JdGatewayParam();
        gatewayParam.setMerchantNo(merchantNo);
        gatewayParam.setSignMd5Key(merchantKey);
        gatewayParam.setGatewayUrl(feConfig.getRefundQueryUrl().getVal());
        JdRefundRespParam resp = jdTradeRemoteService.doRefundQueryStatus(reqParam, gatewayParam);
        if (null != resp) {
            if ("Y".equals(resp.getIsSuccess())) {
                String status = resp.getStatus();
                if (paymentEntity.getSeqId().equals(resp.getOrderNo())) {
                    if ("5".equals(status)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_SUCCESS, "", "REFUND_SUCCESS", "退款成功");
                    } else if ("6".equals(status)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", "REFUND_FAIL", "退款失败");
                    } else if ("4".equals(status)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND, "", "REFUND", "退款中");
                    }
                }
            } else if ("N".equals(resp.getIsSuccess())) {
                PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", resp.getErrorCode(), resp.getErrorDes());
            }
        }
    }

}
