package com.iboxpay.settlement.gateway.alipay.service.payment;


import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.alipay.AlipayFrontEndConfig;
import com.iboxpay.settlement.gateway.alipay.service.callback.CallbackPayment_QrTrade;
import com.iboxpay.settlement.gateway.alipay.service.query.QueryPayment_AlipayQrTrade;
import com.iboxpay.settlement.gateway.alipay.service.refund.RefundPayment_QrTrade;
import com.iboxpay.settlement.gateway.alipay.service.reverse.ReversePayment_QrTrade;
import com.iboxpay.settlement.gateway.alipay.servie.api.AlipayQrTradeRemoteService;
import com.iboxpay.settlement.gateway.alipay.servie.model.AlipayGatewayParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.PreCreateReqParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.PreCreateRespParam;
import com.iboxpay.settlement.gateway.alipay.servie.utils.AlipayExtendsCryptUtils;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.callback.ICallBackPayment;
import com.iboxpay.settlement.gateway.common.trans.close.IClosePayment;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.Sequence;


@Service
public class Payment_AlipayQrTrade implements IPaymentAlipay{
	private static Logger logger = LoggerFactory.getLogger(Payment_AlipayQrTrade.class);

	@Resource
	private AlipayQrTradeRemoteService alipayQrTradeRemoteService;

	@Override
	public void pay(PaymentEntity[] payments,AlipayFrontEndConfig config) throws BaseTransException {
		PaymentEntity paymentEntity=payments[0];
		Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
		// 交易商户号
		String payMerchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String payMerchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		// 子商户号(代理商编号)
		String payMerchantSubNo=String.valueOf(merchantMap.get("payMerchantSubNo"));
		
		PreCreateReqParam param=new PreCreateReqParam(); 
        // 接口名称
        param.setService(config.getNativePrecreateService().getVal());
        // 合作者身份ID
        param.setPartner(payMerchantNo);
        // 参数编码字符集
        param.set_input_charset(config.getCharset().getVal());
        // 签名类型:支付宝请求签名类型 1 证书签名； 2 其他密钥签名，默认为2
        param.setAlipay_ca_request(config.getAlipayCaRequest().getVal());

        
		String merchantNo=(String) paymentEntity.getExtProperty("merchantNo");
		Map<String, Object> extendParamMap = new HashMap<String, Object>();
		// 支付宝代理商ID
        extendParamMap.put("AGENT_ID", payMerchantSubNo);
        // 店铺类型
        extendParamMap.put("STORE_TYPE", "1");//
        // 盒子商户号（门店编号）
        extendParamMap.put("STORE_ID", AlipayExtendsCryptUtils.encode(merchantNo));
        // 扩展参数
		param.setExtend_params(JsonUtil.toJson(extendParamMap));
		
		
		// 回调地址
		param.setNotify_url(config.getNotifyUrl().getVal());
		param.setOperator_code("1");//?
		// 商户订单号
		param.setOut_trade_no(paymentEntity.getSeqId());
		// 订单金额
		param.setTotal_fee(paymentEntity.getAmount().toString());
		// 订单标题
		param.setSubject(paymentEntity.getSeqId());
		// 订单业务类型:QR_CODE_OFFLINE（二维码支付）
		param.setProduct_code("QR_CODE_OFFLINE");
		// 订单支付超时时间
		param.setIt_b_pay("10m");//?
		
		
		String signType =config.getSignType().getVal();
		String gatewayUrl=config.getProtocal().getVal()+"://"+config.getIp().getVal()+config.getUri().getVal();
		String requestTimeout="20";
		// 支付宝网关请求参数
		AlipayGatewayParam gatewayParam =new AlipayGatewayParam();
		gatewayParam.setGatewayUrl(gatewayUrl);
		gatewayParam.setSignMd5Key(payMerchantKey);
		gatewayParam.setSignType(signType);
		gatewayParam.setRequestTimeout(requestTimeout);
		
		// 调用支付宝预下单接口服务
		PreCreateRespParam respParam=alipayQrTradeRemoteService.doPreCreate(param,gatewayParam);
		  if (respParam != null) {
	            // 成功
	            if ("T".equals(respParam.getIs_success())) {
	            	String outTradeNo=respParam.getOut_trade_no();
	            	
	            	String codeUrl=respParam.getQr_code();
	            	String codeImgUrl=respParam.getSmall_pic_url();
	            	String bigImgUrl=respParam.getPic_url();
	            	Map<String,Object> extParam=new HashMap<String,Object>();
	            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl, codeUrl);
	            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl, codeImgUrl);
	            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_BigImgUrl, bigImgUrl);
					// 扩展属性存放json格式数据
					String callbackExtProperties=JsonUtil.toJson(extParam);
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUBMITTED, "", "SUCCESS", "提交成功",callbackExtProperties);
	            }else{
	            	String errCode=null;
	            	String errMsg=null;
	            	PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errMsg);
	            }
	        }
	}
}

