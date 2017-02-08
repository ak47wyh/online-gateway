package com.iboxpay.settlement.gateway.alipay.service.payment;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.alipay.AlipayFrontEndConfig;
import com.iboxpay.settlement.gateway.alipay.service.AlipayContranst;
import com.iboxpay.settlement.gateway.alipay.service.callback.CallbackPayment_QrTrade;
import com.iboxpay.settlement.gateway.alipay.service.query.QueryPayment_AlipayTrade;
import com.iboxpay.settlement.gateway.alipay.service.refund.RefundPayment_QrTrade;
import com.iboxpay.settlement.gateway.alipay.service.reverse.ReversePayment_QrTrade;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.callback.ICallBackPayment;
import com.iboxpay.settlement.gateway.common.trans.close.IClosePayment;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.util.Sequence;

@Service
public class Payment_AlipayTrade implements IPayment{
	
	private static Logger logger = LoggerFactory.getLogger(Payment_AlipayTrade.class);
	public static final String BANK_TRANS_CODE = "alipay";
	public static final String BANK_TRANS_DESC = "支付宝支付";
	
	@Resource
	private Payment_AlipayQrTrade alipayQrTrade;
	
	@Resource
	private Payment_AlipayWapTrade alipayWapTrade;
	
	@Override
	public TransCode getTransCode() {
		return TransCode.PAY;
	}

	@Override
	public String getBankTransCode() {
		return BANK_TRANS_CODE;
	}
	@Override
	public String getBankTransDesc() {
		return BANK_TRANS_DESC;
	}

	@Override
	public PaymentNavigation navigate() {
		return PaymentNavigation.create()
				.setBatchSize(1)//单笔
				.setDiffBank(true)//跨行
				.setSameBank(true)
				.setToPrivate(true)
				.setToCompany(true)//对公,对私都支持
				.setType(PaymentNavigation.Type.online);
	}

	@Override
	public boolean navigateMatch(PaymentEntity payment) {
		return true;
	}

	@Override
	public String check(PaymentEntity[] payments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void genBankBatchSeqId(PaymentEntity[] payments) {
		String bankBatchSeqId = Sequence.genNumberSequence(10);
		for (PaymentEntity payment : payments) {
			payment.setBankBatchSeqId(bankBatchSeqId);
		}
		
	}
	@Override
	public void genBankSeqId(PaymentEntity[] payments) {
		for (int i = 0; i <payments.length; i++) {
			String bankSeqId =payments[i].getSeqId();
			payments[i].setBankSeqId(String.valueOf(bankSeqId));
		}
	}

	@Override
	public void pay(PaymentEntity[] payments) throws BaseTransException {
		AlipayFrontEndConfig alipayConfig = (AlipayFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		PaymentEntity paymentEntity=payments[0];
		String appType= paymentEntity.getAppType();
		if(appType.equals(AlipayContranst.PAY_TYPE_NATIVE)){//扫码支付
			alipayQrTrade.pay(payments,alipayConfig);
		} else if (appType.equals(AlipayContranst.PAY_TYPE_MICROPAY)){//刷卡支付
			
		} else if (appType.equals(AlipayContranst.PAY_TYPE_WAP)){//手机网站支付
			alipayWapTrade.pay(payments,alipayConfig);
		}
	}


	
	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment_AlipayTrade.class;
	}
	@Override
	public Class<? extends IRefundPayment> getRefundClass() {
		return RefundPayment_QrTrade.class;
	}
	@Override
	public Class<? extends IRefundQueryPayment> getRefundQueryClass() {
		return null;
	}
	@Override
	public Class<? extends IReversePayment> getReverseClass() {
		return ReversePayment_QrTrade.class;
	}
	@Override
	public Class<? extends IClosePayment> getCloseClass() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Class<? extends ICallBackPayment> getCallBackClass() {
		return CallbackPayment_QrTrade.class;
	}

	
}
