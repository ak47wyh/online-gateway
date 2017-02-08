package com.iboxpay.settlement.gateway.wft.service.payment;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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
import com.iboxpay.settlement.gateway.wft.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wft.service.callback.Callback_Native;
import com.iboxpay.settlement.gateway.wft.service.close.ClosePayment_Native;
import com.iboxpay.settlement.gateway.wft.service.query.QueryPayment_WechatWft;
import com.iboxpay.settlement.gateway.wft.service.refund.RefundPayment_Native;
import com.iboxpay.settlement.gateway.wft.service.refund.query.QueryRefundPayment_Native;
import com.iboxpay.settlement.gateway.wft.service.reverse.ReversePayment_Micropay;


@Service
public class Payment_WechatWft implements IPayment{
	private static Logger logger = LoggerFactory.getLogger(Payment_WechatWft.class);
	public static final String BANK_TRANS_CODE = "wftWechat";
	public static final String BANK_TRANS_DESC = "微信支付【威富通】";
	
	@Resource
	private Payment_Native paymentNative;
	
	@Resource
	private Payment_Micropay paymentMicropay;

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
        String bankBatchSeqId = Sequence.genSequence();
        for (PaymentEntity payment : payments){
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
		PaymentEntity paymentEntity=payments[0];
		String appType= paymentEntity.getAppType();
		if(appType.equals(WeChatContrants.PAY_TYPE_NATIVE)){//扫码支付
			paymentNative.pay(payments);
		}else if(appType.equals(WeChatContrants.PAY_TYPE_MICROPAY)){//刷卡支付
			paymentMicropay.pay(payments);
		}
	}

	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment_WechatWft.class;
	}

	@Override
	public Class<? extends IRefundPayment> getRefundClass() {
		return RefundPayment_Native.class;
	}

	@Override
	public Class<? extends IRefundQueryPayment> getRefundQueryClass() {
		return QueryRefundPayment_Native.class;
	}

	@Override
	public Class<? extends IReversePayment> getReverseClass() {
		return ReversePayment_Micropay.class;
	}

	@Override
	public Class<? extends IClosePayment> getCloseClass() {
		return ClosePayment_Native.class;
	}

    
    /**
     * 异步回调接口实现类[扫码支付]
     * @return
     */
    public  Class<? extends ICallBackPayment> getCallBackClass(){
    	return Callback_Native.class;
    }
	

}
