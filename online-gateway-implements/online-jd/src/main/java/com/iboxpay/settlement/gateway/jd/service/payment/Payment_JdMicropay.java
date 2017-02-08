package com.iboxpay.settlement.gateway.jd.service.payment;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.jd.JdpayFrontEndConfig;
import com.iboxpay.settlement.gateway.jd.service.api.JdMicropayRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdMicropayReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdMicropayRespParam;

@Service
public class Payment_JdMicropay implements IPaymentJdTrade {

    private static Logger logger = LoggerFactory.getLogger(Payment_JdMicropay.class);
    @Resource
    private JdMicropayRemoteService jdMicropayRemoteService;

    @Override
    public void pay(PaymentEntity[] payments, JdpayFrontEndConfig jdConfig) throws BaseTransException {
        PaymentEntity paymentEntity = payments[0];
        Map<String, Object> merchantMap = paymentEntity.getMerchantMap();
        // 交易商户号
        String merchantNo = String.valueOf(merchantMap.get("payMerchantNo"));
        // 交易秘钥
        String merchantKey = String.valueOf(merchantMap.get("payMerchantKey"));
        // 子商户号
        String merchantSubNo = String.valueOf(merchantMap.get("payMerchantSubNo"));

        JdMicropayReqParam param = new JdMicropayReqParam();
        param.setOrderNo(paymentEntity.getBankSeqId());
        String authCode = (String) paymentEntity.getExtProperty("authCode");
        String productInfo = (String) paymentEntity.getExtProperty("productInfo");
        String productBody = (String) paymentEntity.getExtProperty("productBody");
        String subMer = (String) paymentEntity.getExtProperty("merchantNo");
        String termNo = (String) paymentEntity.getExtProperty("machineNo");
        param.setSeed(authCode);
        param.setNotifyUrl(jdConfig.getNotifyUrl().getVal());
        param.setAmount(paymentEntity.getAmount());
        param.setTradeName(productInfo);
        param.setTradeDescrible(productBody);
        param.setSubMer(subMer);
        param.setTermNo(termNo);
        /**
         * 商户号
         */
        param.setMerchantNo(merchantNo);
        param.setSignMd5Key(merchantKey);
        String gatewayUrl = jdConfig.getMicropayPayUrl().getVal();
        param.setGatewayUrl(gatewayUrl);
        JdMicropayRespParam resp = jdMicropayRemoteService.doMicroPay(param);
        if (resp.getIsSuccess().equals("Y")) {
            PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "", "SUCCESS", "支付成功");
        } else {
            String errCode = resp.getErrorCode();
            String errCodeDes = resp.getErrorDes();
            PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errCodeDes);
        }

    }

}
