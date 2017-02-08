package com.iboxpay.settlement.gateway.jd.service.refund;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.jd.JdpayFrontEndConfig;
import com.iboxpay.settlement.gateway.jd.service.api.JdTradeRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdGatewayParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdRefundReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdRefundRespParam;

@Service
public class RefundPayment_JdTrade implements IRefundPayment {

    public static final String BANK_TRANS_CODE = "refundJdTrade";

    @Resource
    private JdTradeRemoteService jdTradeRemoteService;

    @Override
    public TransCode getTransCode() {
        return TransCode.REFUND;
    }

    @Override
    public String getBankTransCode() {
        return BANK_TRANS_CODE;
    }

    @Override
    public String getBankTransDesc() {
        return "京东刷卡退款";
    }

    @Override
    public void refund(PaymentEntity[] payments) throws BaseTransException, IOException {
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
        //退款单号
        String refundNo = (String) paymentEntity.getExtProperty("outRefundNo");
        JdRefundReqParam reqParam = new JdRefundReqParam();
        reqParam.setOrderNo(paymentEntity.getSeqId());
        reqParam.setRefundAmount(paymentEntity.getAmount().toString());
        reqParam.setRefundNo(refundNo);
        JdGatewayParam gatewayParam = new JdGatewayParam();
        gatewayParam.setGatewayUrl(feConfig.getRefundUrl().getVal());
        gatewayParam.setMerchantNo(merchantNo);
        gatewayParam.setSignMd5Key(merchantKey);
        gatewayParam.setNotifyUrl(feConfig.getNotifyUrl().getVal());
        JdRefundRespParam resp = jdTradeRemoteService.doRefundTrade(reqParam, gatewayParam);
        if (null != resp) {
            if ("Y".equals(resp.getIsSuccess())) {
                String status = resp.getStatus();
                String _refundNo = resp.getRefundNo();

                // 返回扩展参数：退款订单号
                Map<String,Object> extParam=new HashMap<String,Object>();
        		extParam.put("outRefundNo", _refundNo);
        		String callbackExtProperties=JsonUtil.toJson(extParam);
        		
                if (paymentEntity.getSeqId().equals(resp.getOrderNo())) {
                    if ("0".equals(status)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_SUCCESS, "", "REFUND_SUCCESS", "退单成功",callbackExtProperties);
                    } else if ("1".equals(status)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", "REFUND_FAIL", "退单失败",callbackExtProperties);
                    } else if ("2".equals(status)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND, "", "REFUND", "退单中",callbackExtProperties);
                    }
                }
            } else if ("N".equals(resp.getIsSuccess())) {
                PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", resp.getErrorCode(), resp.getErrorDes());
            }
        }

    }

}
