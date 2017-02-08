package com.iboxpay.settlement.gateway.jd.service.callback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.callback.CallbackPaymentRequestModel;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.callback.ICallBackPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.jd.service.api.JdQrTradeRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdNotifyVerifyReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdNotifyVerifyRespParam;

@Service
public class CallbackPayment_JdTrade implements ICallBackPayment {

    private static Logger logger = LoggerFactory.getLogger(CallbackPayment_JdTrade.class);
    public static final String BANK_TRANS_CODE = "callbackNative";

    @Resource
    private JdQrTradeRemoteService jdQrTradeRemoteService;

    @Override
    public TransCode getTransCode() {
        return TransCode.CALLBACK;
    }

    @Override
    public String getBankTransCode() {
        return BANK_TRANS_CODE;
    }

    @Override
    public String getBankTransDesc() {
        return "京东钱包回调处理";
    }

    @Override
    public void doCallback(PaymentEntity payment, CommonRequestModel requestModel) throws BaseTransException, IOException {
        CallbackPaymentRequestModel callbackRequestModel = (CallbackPaymentRequestModel) requestModel;
        String merchantExtProperties = payment.getMerchantExtProperties();
        Map<String, Object> merchantMap = JsonUtil.parseJSON2Map(merchantExtProperties);
        // 交易秘钥
        String merchantKey = String.valueOf(merchantMap.get("payMerchantKey"));
        Map<String, Object> resultMap = callbackRequestModel.getResultMap();
        if (null == resultMap) {
            return;
        }
        String sign = resultMap.get("sign").toString();
        String data = resultMap.get("data").toString();
        String callbackCode = resultMap.get("callbackCode").toString();

        JdNotifyVerifyReqParam reqParam = new JdNotifyVerifyReqParam();
        reqParam.setSign(sign);
        reqParam.setData(data);
        reqParam.setMd5Key(merchantKey);
        JdNotifyVerifyRespParam respParam = jdQrTradeRemoteService.doNotifyVerify(reqParam);
        if ("pay".equals(callbackCode)) {
            if (respParam.getStatus() == 0) {
                String callbackExtProperties = "";
                PaymentStatus.setStatus(payment, PaymentStatus.STATUS_SUCCESS, "", "SUCCESS", "付款成功", callbackExtProperties);
            } else if (respParam.getStatus() == 1) {
                PaymentStatus.setStatus(payment, PaymentStatus.STATUS_FAIL, "", "FAIL", "交易失败");
            }
        } else if ("refund".equals(callbackCode)) {
            Map<String, Object> extParam = new HashMap<String, Object>();
            extParam.put("outRefundNo", respParam.getRefundNo());
            String callbackExtProperties = JsonUtil.toJson(extParam);
            if (respParam.getStatus() == 0) {
                PaymentStatus.setStatus(payment, PaymentStatus.STATUS_REFUND_SUCCESS, "", "REFUND_SUCCESS", "退款成功", callbackExtProperties);
            } else if (respParam.getStatus() == 1) {
                PaymentStatus.setStatus(payment, PaymentStatus.STATUS_REFUND_FAIL, "", "REFUND_FAIL", "退款失败", callbackExtProperties);
            }
        }
        logger.info("京东钱包支付回调处理完成");
    }
}