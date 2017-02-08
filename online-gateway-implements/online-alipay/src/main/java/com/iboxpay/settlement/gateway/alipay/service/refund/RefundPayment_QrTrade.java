package com.iboxpay.settlement.gateway.alipay.service.refund;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.alipay.AlipayFrontEndConfig;
import com.iboxpay.settlement.gateway.alipay.servie.api.AlipayQrTradeRemoteService;
import com.iboxpay.settlement.gateway.alipay.servie.model.AlipayGatewayParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.RefundTradeReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.RefundTradeRespParam;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;

@Service
public class RefundPayment_QrTrade implements IRefundPayment{
	private static Logger logger = LoggerFactory.getLogger(RefundPayment_QrTrade.class);
	public static final String BANK_TRANS_CODE = "refundQrTrade";
	
	@Resource
	private AlipayQrTradeRemoteService alipayQrTradeRemoteService;
	
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
		return "支付宝扫码退款";
	}

	@Override
	public void refund(PaymentEntity[] payments) throws BaseTransException, IOException {
		PaymentEntity paymentEntity = payments[0];
		AlipayFrontEndConfig config = (AlipayFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String merchantExtProperties=paymentEntity.getMerchantExtProperties();
		Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExtProperties);
		// 交易商户号【合作者ID】
		String payMerchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String payMerchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		
		RefundTradeReqParam reqParam = new RefundTradeReqParam();
		// 接口名称
		reqParam.setService(config.getNativeRefundService().getVal());
		// 合作者身份ID
		reqParam.setPartner(payMerchantNo);
		// 参数编码字符集
		reqParam.set_input_charset(config.getCharset().getVal());
		// 签名类型:支付宝请求签名类型 1 证书签名； 2 其他密钥签名，默认为2
		reqParam.setAlipay_ca_request(config.getAlipayCaRequest().getVal());
		// 订单号
		reqParam.setOut_trade_no(paymentEntity.getSeqId());
		// 退款金额
		reqParam.setRefund_amount(paymentEntity.getAmount().toString());
		
		String signType =config.getSignType().getVal();
		String gatewayUrl=config.getProtocal().getVal()+"://"+config.getIp().getVal()+config.getUri().getVal();
		String requestTimeout="20";
		// 支付宝网关请求参数
		AlipayGatewayParam gatewayParam =new AlipayGatewayParam();
		gatewayParam.setGatewayUrl(gatewayUrl);
		gatewayParam.setSignMd5Key(payMerchantKey);
		gatewayParam.setSignType(signType);
		gatewayParam.setRequestTimeout(requestTimeout);
		
		RefundTradeRespParam resp = alipayQrTradeRemoteService.doRefundTrade(reqParam, gatewayParam);

		if (resp != null) {
			String isSuccess = resp.getIs_success();
			if (isSuccess.equals("T")) {
				String resultCode = resp.getResult_code();
				if (resultCode.equals("SUCCESS")) {
					String outTradeNo = resp.getOut_trade_no();
					if (paymentEntity.getSeqId().equals(outTradeNo)) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_SUCCESS, "", "SUCCESS", "退款成功");
					}
				} else if(resultCode.equals("FAIL")){
					String errCode = resp.getDetail_error_code();
					String errMsg = resp.getDetail_error_des();
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", errCode, errMsg);
				} else if(resultCode.equals("UNKNOWN")){
					String errCode = resp.getDetail_error_code();
					String errMsg = resp.getDetail_error_des();
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", errCode, errMsg);
				}

			}
		}
		
	}

}
