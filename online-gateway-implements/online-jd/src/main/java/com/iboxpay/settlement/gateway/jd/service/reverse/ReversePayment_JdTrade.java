package com.iboxpay.settlement.gateway.jd.service.reverse;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.jd.JdpayFrontEndConfig;
import com.iboxpay.settlement.gateway.jd.service.api.JdTradeRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdCancleReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdCancleRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdGatewayParam;

@Service
public class ReversePayment_JdTrade implements IReversePayment {

    private final static String TRANS_CODE_CANCLE = "reversePayment";

    private final static String TRANS_CODE_CANCLE_DESC = "京东订单撤消";
    @Resource
    private JdTradeRemoteService jdTradeRemoteService;

    @Override
    public TransCode getTransCode() {
        // TODO Auto-generated method stub
        return TransCode.REVERSE;
    }

    @Override
    public String getBankTransCode() {
        // TODO Auto-generated method stub
        return TRANS_CODE_CANCLE;
    }

    @Override
    public String getBankTransDesc() {
        // TODO Auto-generated method stub
        return TRANS_CODE_CANCLE_DESC;
    }

    @Override
    public void reverse(PaymentEntity[] payments) throws BaseTransException, IOException {
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
        //撤消请求号
        String cancleNo = (String) paymentEntity.getExtProperty("cancleNo");

        JdCancleReqParam reqParam = new JdCancleReqParam();
        reqParam.setOrderNo(paymentEntity.getSeqId());
        reqParam.setAmount(paymentEntity.getAmount().toString());
        reqParam.setCancleNo(cancleNo);

        JdGatewayParam gatewayParam = new JdGatewayParam();
        gatewayParam.setGatewayUrl(feConfig.getReverseUrl().getVal());
        gatewayParam.setMerchantNo(merchantNo);
        gatewayParam.setSignMd5Key(merchantKey);
        JdCancleRespParam resp = jdTradeRemoteService.doCancelTrade(reqParam, gatewayParam);
        if (null != resp) {
            if ("Y".equals(resp.getIsSuccess())) {
                String status = resp.getStatus();
                // 处理自己的支付逻辑
                if ("0".equals(status)) {
                    PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REVERSE, "", "SUCCESS", "冲正成功");
                } else if ("1".equals(status)) {
                    PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REVERSE_FAIL, "", "FAIL", "冲正失败");
                }
            } else if ("N".equals(resp.getIsSuccess())) {
                PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", resp.getErrorCode(), resp.getErrorDes());
            }
        }
    }
}
