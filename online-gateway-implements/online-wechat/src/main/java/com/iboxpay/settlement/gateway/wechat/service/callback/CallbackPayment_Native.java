package com.iboxpay.settlement.gateway.wechat.service.callback;

import java.io.IOException;
import java.util.Map;
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
import com.iboxpay.settlement.gateway.wechat.service.PaymentWechatService;
import com.iboxpay.settlement.gateway.wechat.service.refund.RefundPayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.utils.SignUtils;

@Service
public class CallbackPayment_Native implements ICallBackPayment{
	private static Logger logger = LoggerFactory.getLogger(RefundPayment_Native.class);
	public static final String BANK_TRANS_CODE = "callbackNative";
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
		System.out.println("微信扫码支付回调处理");
		CallbackPaymentRequestModel model = (CallbackPaymentRequestModel) requestModel;
		
		String merchantExtProperties=payment.getMerchantExtProperties();
		Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExtProperties);
		// 交易秘钥
		String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		
		// 解析异步返回参数
		Map map=model.getResultMap();
		if (map.containsKey("sign")) {
			if (!SignUtils.checkParam(map, merchantKey)) {
				logger.info("验证签名不通过");
			} else {
				String return_code = (String) map.get("return_code");
				if (return_code != null && "SUCCESS".equals(return_code)) {
					String result_code = (String) map.get("result_code");
					String out_trade_no = (String) map.get("out_trade_no");// 商户订单号
					String errCode = (String) map.get("err_code");
					String errMsg = (String) map.get("err_code_des");
					
					if (result_code != null && "SUCCESS".equals(result_code)&&out_trade_no.equals(payment.getBankSeqId())) {
						String callbackExtProperties = PaymentWechatService.initCallbackExtProperties(map,payment);
						PaymentStatus.setStatus(payment,PaymentStatus.STATUS_SUCCESS, "", result_code,"付款成功",callbackExtProperties);
					}else if("FAIL".equals(result_code)) {
						PaymentStatus.setStatus(payment,PaymentStatus.STATUS_FAIL, "", errCode,errMsg);
					}
				}
			}
		}
		System.out.println("微信扫码支付回调处理完成");
	}

}
