package com.iboxpay.settlement.gateway.alipay.service.payment;

import java.util.Map;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.alipay.AlipayFrontEndConfig;
import com.iboxpay.settlement.gateway.alipay.servie.api.AlipayWapTradeRemoteService;
import com.iboxpay.settlement.gateway.alipay.servie.model.AlipayGatewayParam;
import com.iboxpay.settlement.gateway.alipay.servie.model.WapOrderReqParam;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Service
public class Payment_AlipayWapTrade implements IPaymentAlipay{
	
	@Resource
	private AlipayWapTradeRemoteService alipayWapTradeRemoteService;
	
	public void pay(PaymentEntity[] payments,AlipayFrontEndConfig alipayConfig) throws BaseTransException {
		PaymentEntity paymentEntity=payments[0];
		
		Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
		// 交易商户号
		String payMerchantNo=String.valueOf(merchantMap.get("payMerchantNo"));
		// 交易秘钥
		String payMerchantKey=String.valueOf(merchantMap.get("payMerchantKey"));		
		
		WapOrderReqParam param =new WapOrderReqParam();
        // 接口名称
		param.setService(alipayConfig.getWapCreateUserService().getVal());
        // 合作者身份ID
		param.setPartner(payMerchantNo);
		// 卖家支付宝用户号
		param.setSeller_id(payMerchantNo);
		// 参数编码字符集
		param.set_input_charset(alipayConfig.getCharset().getVal());
		// 支付类型。仅支持：1（商品购买）
		param.setPayment_type(alipayConfig.getPaymentType().getVal());
		// 服务器异步通知页面路径
		param.setNotify_url(alipayConfig.getWapNotifyUrl().getVal());
		// 页面跳转同步通知页面路径
		param.setReturn_url(alipayConfig.getWapReturnUrl().getVal());
		// 商户网站唯一订单号
		param.setOut_trade_no(paymentEntity.getSeqId());
        // 交易金额
		param.setTotal_fee(paymentEntity.getAmount().toString());	
		// 商品展示网址
		String showUrl=(String) paymentEntity.getExtProperty("showUrl");
		// 订单名称
		String orderTitle=(String) paymentEntity.getExtProperty("orderTitle");
		// 商品描述
		String orderBody=(String) paymentEntity.getExtProperty("orderBody");
		param.setShow_url(showUrl);
		if (!StringUtils.isEmpty(orderTitle)){
			param.setSubject(orderTitle);
		} else {
			param.setSubject(paymentEntity.getSeqId());
		}
		param.setBody(orderBody);
		
		
		String signType =alipayConfig.getSignType().getVal();
		String gatewayUrl=alipayConfig.getProtocal().getVal()+"://"+alipayConfig.getIp().getVal()+alipayConfig.getUri().getVal();
		String requestTimeout="20";
		// 支付宝网关请求参数
		AlipayGatewayParam gatewayParam =new AlipayGatewayParam();
		gatewayParam.setGatewayUrl(gatewayUrl);
		gatewayParam.setSignMd5Key(payMerchantKey);
		gatewayParam.setSignType(signType);
		gatewayParam.setRequestTimeout(requestTimeout);
		
		String htmlContext = alipayWapTradeRemoteService.doWapOrder(param,gatewayParam);
		paymentEntity.setHtmlContext(htmlContext);
		
		PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUBMITTED, "", "SUCCESS", "提交成功");
	}
}
