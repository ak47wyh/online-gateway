package com.iboxpay.settlement.gateway.alipay.service.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.alipay.AlipayFrontEndConfig;
import com.iboxpay.settlement.gateway.alipay.servie.api.AlipayQrTradeRemoteService;
import com.iboxpay.settlement.gateway.alipay.servie.model.AlipayGatewayParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.QueryStatusReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.QueryStatusRespParam;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Service
public class QueryPayment_AlipayQrTrade implements IQueryPayment {
	private static Logger logger = LoggerFactory.getLogger(QueryPayment_AlipayQrTrade.class);
	private final static String TRANS_CODE_QUERY_QR = "queryQrTrade";

	@Resource
	private AlipayQrTradeRemoteService alipayQrTradeRemoteService;

	@Override
	public TransCode getTransCode() {
		return TransCode.QUERY;
	}

	@Override
	public String getBankTransCode() {
		// TODO Auto-generated method stub
		return TRANS_CODE_QUERY_QR;
	}

	@Override
	public String getBankTransDesc() {
		return "支付宝扫码支付查询";
	}

	@Override
	public void query(PaymentEntity[] payments) throws BaseTransException, IOException {
		PaymentEntity paymentEntity = payments[0];
		// 获取前置机
		AlipayFrontEndConfig config = (AlipayFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		
		String merchantExtProperties=paymentEntity.getMerchantExtProperties();
		Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExtProperties);
		// 交易商户号【合作者ID】
		String payMerchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String payMerchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		
		QueryStatusReqParam reqParam = new QueryStatusReqParam();
		// 接口名称
		reqParam.setService(config.getNativeQueryService().getVal());
		// 合作者身份ID
		reqParam.setPartner(payMerchantNo);
		// 参数编码字符集
		reqParam.set_input_charset(config.getCharset().getVal());
        // 签名类型:支付宝请求签名类型 1 证书签名； 2 其他密钥签名，默认为2
		reqParam.setAlipay_ca_request(config.getAlipayCaRequest().getVal());		
        // 商家订单号
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
		
		QueryStatusRespParam resp = alipayQrTradeRemoteService.doQueryStatus(reqParam,gatewayParam);
		if (resp != null) {
			String isSuccess = resp.getIs_success();
			if (isSuccess.equals("T")) {
				String resultCode = resp.getResult_code();
				String errCode = resp.getDetail_error_code();
				String errMsg = resp.getDetail_error_des();
				String tradeStatus=resp.getTrade_status();
				if (resultCode.equals("SUCCESS")) {
					String outTradeNo = resp.getOut_trade_no();
					String callbackExtProperties = initCallbackProperties(paymentEntity, resp);
					if (paymentEntity.getSeqId().equals(outTradeNo)) {
						if (tradeStatus.equals("TRADE_SUCCESS")) {
							PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "", "SUCCESS", "支付成功",callbackExtProperties);
						}else if(tradeStatus.equals("TRADE_CLOSED")){
							PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", tradeStatus, "未付款交易超时关闭，或支付完成后全额退款",callbackExtProperties);
						}else if (tradeStatus.equals("TRADE_FINISHED")) {
							PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", tradeStatus, "交易结束，不可退款",callbackExtProperties);
						}else if (tradeStatus.equals("WAIT_BUYER_PAY")) {
							PaymentStatus.setStatus(payments, PaymentStatus.STATUS_WAITTING_PAY, "", tradeStatus, "交易结束，不可退款",callbackExtProperties);
						}
					}
				} else if(errCode.equals("TRADE_NOT_EXIST")){
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "",errCode, errMsg);
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errMsg);
				}

			}

		}

	}

	/**
	 * 初始化回调扩展参数信息
	 * @param paymentEntity
	 * @param resp
	 * @return
	 */
	private String initCallbackProperties(PaymentEntity paymentEntity, QueryStatusRespParam resp) {
		String buyerLoginId = resp.getBuyer_logon_id();
		String buyerId = resp.getBuyer_user_id();
		
		String codeUrl=(String) paymentEntity.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl);
		String codeImgUrl=(String) paymentEntity.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl);
		String bigImgUrl=(String) paymentEntity.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_BigImgUrl);
		Map<String,Object> extParam=new HashMap<String,Object>();
		if(!StringUtils.isEmpty(codeUrl)){
			extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl, codeUrl);
		}
		if(!StringUtils.isEmpty(codeImgUrl)){
			extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl, codeImgUrl);		
		}
		if(!StringUtils.isEmpty(bigImgUrl)){
			extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_BigImgUrl, bigImgUrl);
		}
		if(!StringUtils.isEmpty(buyerId)){
			extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_BUYERID, buyerId);
		}
		if(!StringUtils.isEmpty(buyerLoginId)){
			extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_BUYERLOGINID, buyerLoginId);
		}
		String callbackExtProperties=JsonUtil.toJson(extParam);
		return callbackExtProperties;
	}

}
