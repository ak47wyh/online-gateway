package com.iboxpay.settlement.gateway.alipay.service.callback;

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
import com.iboxpay.settlement.gateway.alipay.servie.model.NotifyVerifyReqParam;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.callback.CallbackPaymentRequestModel;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.callback.ICallBackPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Service
public class CallbackPayment_QrTrade implements ICallBackPayment{
	private static Logger logger = LoggerFactory.getLogger(CallbackPayment_QrTrade.class);
	public static final String BANK_TRANS_CODE = "callbackQrTrade";
	
	@Resource 
	private AlipayQrTradeRemoteService alipayQrTradeRemoteService;
	
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
		return "微信扫码支付回调处理";
	}

	@Override
	public void doCallback(PaymentEntity payment, CommonRequestModel requestModel)throws BaseTransException, IOException {
		// 获取前置机
		AlipayFrontEndConfig config=(AlipayFrontEndConfig) TransContext.getContext().getFrontEndConfig();
	    
		CallbackPaymentRequestModel model = (CallbackPaymentRequestModel) requestModel;
		// 解析异步返回参数
		Map<String,Object> map=model.getResultMap();
		String tradeStatus=String.valueOf(map.get("tradeStatus"));
		String notifyId=String.valueOf(map.get("notifyId"));
		String outTradeNo=String.valueOf(map.get("outTradeNo"));
		String buyerId=String.valueOf(map.get("buyerId"));
		String buyerLoginId=String.valueOf(map.get("buyerLoginId"));
		
		String merchantExtProperties=payment.getMerchantExtProperties();
		Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExtProperties);
		// 交易商户号【合作者ID】
		String payMerchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String payMerchantKey=String.valueOf(merchantMap.get("payMerchantKey"));

		// 验证当前交易
        NotifyVerifyReqParam reqParam = generateNotifyReqParam(config,payMerchantNo);
        reqParam.setNotify_id(notifyId);
        
        
		String signType =config.getSignType().getVal();
		String gatewayUrl=config.getProtocal().getVal()+"://"+config.getIp().getVal()+config.getUri().getVal();
		String requestTimeout="20";
		// 支付宝网关请求参数
		AlipayGatewayParam gatewayParam =new AlipayGatewayParam();
		gatewayParam.setGatewayUrl(gatewayUrl);
		gatewayParam.setSignMd5Key(payMerchantKey);
		gatewayParam.setSignType(signType);
		gatewayParam.setRequestTimeout(requestTimeout);
        
        Boolean isFlag= alipayQrTradeRemoteService.doNotifyVerify(reqParam,gatewayParam);

        if(isFlag){
        	if(!StringUtils.isEmpty(tradeStatus)){
            	String codeUrl=(String) payment.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl);
            	String codeImgUrl=(String) payment.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl);
            	String bigImgUrl=(String) payment.getCallbackExtProperties(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl);
            	Map<String,Object> extParam=new HashMap<String,Object>();
            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl, codeUrl);
            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl, codeImgUrl);
            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_BigImgUrl, bigImgUrl);
            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_BUYERID, buyerId);
            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_BUYERLOGINID, buyerLoginId);
            	String callbackExtProperties=JsonUtil.toJson(extParam);
        		
        		/**
        		 * WAIT_BUYER_PAY	交易创建，等待买家付款
        		 * TRADE_CLOSED	未付款交易超时关闭，或支付完成后全额退款
        		 * TRADE_SUCCESS	交易支付成功
        		 * TRADE_FINISHED	交易结束，不可退款
        		 */
				if (tradeStatus.equals("TRADE_SUCCESS")&&outTradeNo.equals(payment.getBankSeqId())) {
					PaymentStatus.setStatus(payment,PaymentStatus.STATUS_SUCCESS, "", tradeStatus,"交易支付成功",callbackExtProperties);
				} else if (tradeStatus.equals("TRADE_CLOSED")) {
					PaymentStatus.setStatus(payment, PaymentStatus.STATUS_CLOSED, "", tradeStatus, "未付款交易超时关闭，或支付完成后全额退款",callbackExtProperties);
				} else if (tradeStatus.equals("TRADE_FINISHED")) {
					PaymentStatus.setStatus(payment, PaymentStatus.STATUS_FAIL, "", tradeStatus, "交易结束，不可退款",callbackExtProperties);
				}
        	}
        }else{
        	logger.error("交易验证失败");
        }
	}
        
        
	private NotifyVerifyReqParam generateNotifyReqParam(AlipayFrontEndConfig config,String  partnerId) {
		String ALIPAY_SERVICE_NOTIFY_VERIFY = config.getNativeNotifyVerify().getVal();
		String ALIPAY_PARTNER_ID = partnerId;
		NotifyVerifyReqParam param = new NotifyVerifyReqParam();
		param.setService(ALIPAY_SERVICE_NOTIFY_VERIFY);
		param.setPartner(ALIPAY_PARTNER_ID);
		return param;
	}

}
