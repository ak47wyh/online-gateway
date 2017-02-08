package com.iboxpay.settlement.gateway.wft.service.query;

import java.io.IOException;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.wft.service.WeChatContrants;

@Service
public class QueryPayment_WechatWft implements IQueryPayment{
	private static Logger logger = LoggerFactory.getLogger(QueryPayment_Native.class);
	private final static String TRANS_CODE_QUERY= "queryWechatWft";
	private final static String TRANS_CODE_QUERY_DESC= "微信支付查询【威富通】";
	
	@Resource
	private QueryPayment_Micropay queryPaymentMicropay;
	@Resource
	private QueryPayment_Native queryPaymentNative;
	
	@Override
	public TransCode getTransCode() {
		return TransCode.QUERY;
	}

	@Override
	public String getBankTransCode() {
		return TRANS_CODE_QUERY;
	}

	@Override
	public String getBankTransDesc() {
		return TRANS_CODE_QUERY_DESC;
	}

	@Override
	public void query(PaymentEntity[] payments) throws BaseTransException, IOException {
		PaymentEntity paymentEntity=payments[0];
		String appType= paymentEntity.getAppType();
		if(appType.equals(WeChatContrants.PAY_TYPE_NATIVE)){//扫码支付
			queryPaymentNative.query(payments);
		}else if(appType.equals(WeChatContrants.PAY_TYPE_MICROPAY)){//刷卡支付
			queryPaymentMicropay.query(payments);
		}
	}

}
