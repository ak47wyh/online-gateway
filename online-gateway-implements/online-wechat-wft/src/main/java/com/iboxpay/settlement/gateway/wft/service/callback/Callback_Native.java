package com.iboxpay.settlement.gateway.wft.service.callback;

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
import com.iboxpay.settlement.gateway.wft.service.PaymentWeChatService;
import com.iboxpay.settlement.gateway.wft.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wft.service.utils.SignUtils;

@Service
public class Callback_Native implements ICallBackPayment{
	private static Logger logger = LoggerFactory.getLogger(Callback_Native.class);
	private final static String BANK_TRANS_CODE = "callbackWftNative";
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
		return "【威富通】微信扫描支付回调函数";
	}

	@Override
	public void doCallback(PaymentEntity payment, CommonRequestModel requestModel)throws BaseTransException, IOException {
		CallbackPaymentRequestModel model = (CallbackPaymentRequestModel) requestModel;
		
		String merchantExt= payment.getMerchantExtProperties();
		Map<String, Object> merchantMap=JsonUtil.parseJSON2Map(merchantExt);
		// 交易秘钥
		String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
		
		// 解析异步返回参数
		Map map=model.getResultMap();
		if (map.containsKey("sign")) {
			if (!SignUtils.checkParam(map, merchantKey)) {
				logger.info("验证签名不通过");
			} else {
				String status = (String) map.get("status");
				if (status != null && WeChatContrants.STATUS_SUCCESS.equals(status)) {
					String result_code = (String) map.get("result_code");
					String errCode = (String) map.get("err_code");
					String errMsg = (String) map.get("err_code_des");
					if(result_code != null && WeChatContrants.RESULT_CODE_SUCCESS.equals(result_code)){
						String out_trade_no = (String) map.get("out_trade_no");// 商户订单号
						String pay_result= (String) map.get("pay_result");//支付结果
						String pay_info= (String) map.get("pay_info");// 支付结果描述
						String callbackExtProperties = PaymentWeChatService.initCallbackExtProperties(map,payment);
						if("0".equals(pay_result)&&out_trade_no.equals(payment.getBankSeqId())){
							PaymentStatus.setStatus(payment,PaymentStatus.STATUS_SUCCESS, "", pay_result,"支付成功",callbackExtProperties);
						}else{
							PaymentStatus.setStatus(payment,PaymentStatus.STATUS_FAIL, "", pay_result,pay_info,callbackExtProperties);
						}
					}else{
						logger.info("交易同步结果错误，批次号："+payment.getBatchSeqId()+"错误代码："+errCode+"错误描述："+errMsg);
					}
				}
			}
		}
	}

}
