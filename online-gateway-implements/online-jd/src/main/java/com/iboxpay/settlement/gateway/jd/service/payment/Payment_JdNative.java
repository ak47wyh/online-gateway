package com.iboxpay.settlement.gateway.jd.service.payment;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.jd.JdpayFrontEndConfig;
import com.iboxpay.settlement.gateway.jd.service.api.JdQrTradeRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdQrReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQrRespParam;

@Service
public class Payment_JdNative implements IPaymentJdTrade{

    @Resource
    private JdQrTradeRemoteService jdQrTradeRemoteService;
	
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
		
        JdQrReqParam param = new JdQrReqParam();
        // 订单号
        param.setOrderNo(paymentEntity.getBankSeqId());
        // 订单金额
        param.setAmount(paymentEntity.getAmount());
        // 回调地址
        param.setNotifyUrl(jdConfig.getNotifyUrl().getVal());
        // 交易摘要
        String productInfo = (String) paymentEntity.getExtProperty("productInfo");
        String productBody = (String) paymentEntity.getExtProperty("productBody");
        param.setTradeName(productInfo);
        // 交易详细描述
        param.setTradeDescrible(productBody);
        // 二维码有效时间 单位为分钟 二维码从生成到扫码支付这个时间段如果超过这个时间，会提示二维码失效
        param.setExpire(5);

		String subMer = (String) paymentEntity.getExtProperty("merchantNo");
		String termNo = (String) paymentEntity.getExtProperty("machineNo");
		// 门店编号
        param.setSubMer(subMer);
        // 机具号
        param.setTermNo(termNo);
        // 商户号
        param.setMerchantNo(merchantNo);
        param.setSignMd5Key(merchantKey);
        String gatewayUrl = jdConfig.getNativePayUrl().getVal();
        param.setGatewayUrl(gatewayUrl);
        // 调用京东钱包下单API实现
        JdQrRespParam resp = jdQrTradeRemoteService.doPreCreate(param);
        if (resp.getIsSuccess().equals("Y")) {
            String qrcode = resp.getQrcode();
            Map<String, Object> extParam = new HashMap<String, Object>();
            extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl, qrcode);
            extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl, "");
            String callbackExtProperties = JsonUtil.toJson(extParam);
            PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUBMITTED, "", "Y", "提交成功", callbackExtProperties);
        } else {
            String errCode = resp.getErrorCode();
            String errCodeDes = resp.getErrorDes();
            PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errCodeDes);
        }
		
	}

}
