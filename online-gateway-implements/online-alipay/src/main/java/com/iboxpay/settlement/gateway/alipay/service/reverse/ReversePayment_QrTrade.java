package com.iboxpay.settlement.gateway.alipay.service.reverse;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.alipay.AlipayFrontEndConfig;
import com.iboxpay.settlement.gateway.alipay.servie.api.AlipayQrTradeRemoteService;
import com.iboxpay.settlement.gateway.alipay.servie.model.AlipayGatewayParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.CancelTradeReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.CancelTradeRespParam;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;



/**
 * 支付宝扫码冲正
 * @author liaoxiongjian
 * @date 2016-2-5 10:18
 */
@Service
public class ReversePayment_QrTrade implements IReversePayment{
	
	private static Logger logger = LoggerFactory.getLogger(ReversePayment_QrTrade.class);
	private final static String TRANS_CODE_REVERSE_QRTRADE= "reverseQrTrade";
	
	@Resource
	private AlipayQrTradeRemoteService alipayQrTradeRemoteService;
	@Override
	public TransCode getTransCode() {
		return TransCode.REVERSE;
	}

	@Override
	public String getBankTransCode() {
		return TRANS_CODE_REVERSE_QRTRADE;
	}

	@Override
	public String getBankTransDesc() {
		return "支付宝撤销";
	}

	@Override
	public void reverse(PaymentEntity[] payments) throws BaseTransException, IOException {
		PaymentEntity paymentEntity = payments[0];
		AlipayFrontEndConfig config = (AlipayFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String merchantExtProperties=paymentEntity.getMerchantExtProperties();
		Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExtProperties);
		// 交易商户号【合作者ID】
		String payMerchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String payMerchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		
		CancelTradeReqParam reqParam = new CancelTradeReqParam();
		// 接口名称
		reqParam.setService(config.getNativeCancelService().getVal());
		// 合作者身份ID
		reqParam.setPartner(payMerchantNo);
		// 参数编码字符集
		reqParam.set_input_charset(config.getCharset().getVal());
		// 签名类型:支付宝请求签名类型 1 证书签名； 2 其他密钥签名，默认为2
		reqParam.setAlipay_ca_request(config.getAlipayCaRequest().getVal());
		// 订单号
		reqParam.setOut_trade_no(paymentEntity.getSeqId());

		
		String signType =config.getSignType().getVal();
		String gatewayUrl=config.getProtocal().getVal()+"://"+config.getIp().getVal()+config.getUri().getVal();
		String requestTimeout="20";
		// 支付宝网关请求参数
		AlipayGatewayParam gatewayParam =new AlipayGatewayParam();
		gatewayParam.setGatewayUrl(gatewayUrl);
		gatewayParam.setSignMd5Key(payMerchantKey);
		gatewayParam.setSignType(signType);
		gatewayParam.setRequestTimeout(requestTimeout);
		
		CancelTradeRespParam resp = alipayQrTradeRemoteService.doCancelTrade(reqParam,gatewayParam);

		if (resp != null) {
			String isSuccess = resp.getIs_success();
			if (isSuccess.equals("T")) {
				String resultCode = resp.getResult_code();
				if (resultCode.equals("SUCCESS")) {
					String outTradeNo = resp.getOut_trade_no();
					if (paymentEntity.getSeqId().equals(outTradeNo)) {
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REVERSE, "", "SUCCESS", "冲正成功");
					}
				} else {
					String errCode = resp.getDetail_error_code();
					String errMsg = resp.getDetail_error_des();
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REVERSE_FAIL, "", errCode, errMsg);
				}

			}
		}
	}


}
