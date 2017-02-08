package com.iboxpay.settlement.gateway.kq.service.payment;


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bill99.schema.ddp.product.MerchantDebitPkiRequest;
import com.bill99.schema.ddp.product.MerchantDebitPkiResponse;
import com.bill99.schema.ddp.product.MerchantSingleDebitResponse;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.payment.AbstractPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.kq.KQFrontEndConfig;
import com.iboxpay.settlement.gateway.kq.KqAccountEntityExt;
import com.iboxpay.settlement.gateway.kq.entity.DealInfoEntity;
import com.iboxpay.settlement.gateway.kq.service.KQConstants;
import com.iboxpay.settlement.gateway.kq.service.PaymentKqService;
import com.iboxpay.settlement.gateway.kq.service.api.CustomerTool;
import com.iboxpay.settlement.gateway.kq.service.query.QueryPayment_Single;
import com.iboxpay.settlement.gateway.kq.service.util.CustomerUtil;
import com.iboxpay.settlement.gateway.kq.service.util.StringUtils;


/**
 * 快钱代收单笔支付
 * 
 * @author liaoxiongjian
 * @date 2015-12-11 10:25
 */
@Service
public class Payment_Single extends AbstractPayment{
	private static Logger logger = LoggerFactory.getLogger(Payment_Batch.class);
	private final static String TRANS_CODE_KQ = "kqSingle"; // 设置银行支付简码
	@Override
	public PaymentNavigation navigate() {
		return PaymentNavigation.create()
				.setBatchSize(1)
				.setToPrivate(true)// 对私
				.setToCompany(true)// 先弄成支持吧
				.setDiffBank(true);
	}

	@Override
	public String check(PaymentEntity[] payments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment_Single.class;// 查询
	}

	@Override
	public String getBankTransCode() {
		return TRANS_CODE_KQ;
	}

	@Override
	public String getBankTransDesc() {
		return "免签约单笔代扣";
	}
	
	@Override
	public void genBankBatchSeqId(PaymentEntity[] payments) {
		String bankBatchSeqId = Sequence.genNumberSequence(15);
		for (PaymentEntity payment : payments) {
			payment.setBankBatchSeqId(bankBatchSeqId);
		}
	}
	
	@Override
	public void genBankSeqId(PaymentEntity[] payments) {
		for (int i = 1; i <= payments.length; i++) {
			String bankSeqId = Sequence.gen16CharSequence();
			payments[i - 1].setBankSeqId(String.valueOf(bankSeqId));
		}
	}
	
	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
		// 获取前置机的配置信息
		TransContext context = TransContext.getContext();
		KQFrontEndConfig kqConfig = (KQFrontEndConfig) context.getFrontEndConfig();
		KqAccountEntityExt  account = (KqAccountEntityExt)TransContext.getContext().getMainAccount();
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		DealInfoEntity dealInfo = PaymentKqService.initSinglePayData(paymentEntity,kqConfig,account);
		
		// 提交请求数据入口
		CustomerTool ct = new CustomerTool();
		MerchantDebitPkiRequest request = ct.getMerchantDebitPkiRequest(dealInfo);
		String postContent = StringUtils.ReqFormat(CustomerUtil.merchantDebitPkiRequestToXml(request));
		return postContent;
	}

	@Override
	public void parse(String respStr, PaymentEntity[] payments)throws ParseMessageException {
		// 获取前置机的配置信息
		TransContext context = TransContext.getContext();
		KQFrontEndConfig kqConfig = (KQFrontEndConfig) context.getFrontEndConfig();
		KqAccountEntityExt  account = (KqAccountEntityExt)TransContext.getContext().getMainAccount();
		// 解析返回数据
		String responseXML = StringUtils.ResFormat(respStr);
		// 把xml转换为MerchantDebitPkiResponse 
		MerchantDebitPkiResponse response = CustomerUtil.xmlToMerchantDebitPkiResponse(responseXML);
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		DealInfoEntity dealInfo = PaymentKqService.initSinglePayData(paymentEntity,kqConfig,account);
		
		// 解密返回数据
		CustomerTool ct = new CustomerTool();
		MerchantSingleDebitResponse bsar = (MerchantSingleDebitResponse) ct.unseal(response, dealInfo);
		
		String failCode=bsar.getFailCode();
		String failreason=bsar.getFailreason();
		String dealResult=bsar.getDealResult();
		if(dealResult.equals(KQConstants.PAYMENT_SATUS_01001)){
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "", bsar.getDealResult(), "扣款成功");
		} else if(dealResult.equals(KQConstants.PAYMENT_SATUS_01002)||dealResult.equals(KQConstants.PAYMENT_SATUS_01004)){
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", failCode, failreason);
		}else {//其它情况通过查询接口确定
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", failCode, failreason);
		}
		
	}


	
	
	@Override
	public Map<String, String> getHeaderMap() {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "text/xml; charset=utf-8");
		return headerMap;
	}

	@Override
	protected String getUri() {
		return ((KQFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getUri().getVal();
	}
}
