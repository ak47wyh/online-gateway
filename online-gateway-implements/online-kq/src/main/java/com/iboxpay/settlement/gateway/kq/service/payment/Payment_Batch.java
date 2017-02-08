package com.iboxpay.settlement.gateway.kq.service.payment;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.bill99.schema.ddp.product.MerchantDebitPkiRequest;
import com.bill99.schema.ddp.product.MerchantDebitPkiResponse;
import com.bill99.schema.ddp.product.MerchantDebitResponse;
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
import com.iboxpay.settlement.gateway.kq.service.query.QueryPayment_Batch;
import com.iboxpay.settlement.gateway.kq.service.util.CustomerUtil;
import com.iboxpay.settlement.gateway.kq.service.util.StringUtils;


/**
 * 快钱支付批量代付交易提交处理
 * 
 * @author liaoxiongjian
 * @date 2015-09-29 10:25
 */
@Service
public class Payment_Batch extends AbstractPayment {

	private static Logger logger = LoggerFactory.getLogger(Payment_Batch.class);
	private final static String TRANS_CODE_KQ = "kq"; // 设置银行支付简码
	private final static Integer MAX_BATCH_SIZE = 500; // 设置最大报文提交笔数

	@Override
	public PaymentNavigation navigate() {
		return PaymentNavigation.create().setToPrivate(true)// 对私
				.setToCompany(true)// 先弄成支持吧
				.setSameBank(true) //
				.setDiffBank(true).setBatchSize(MAX_BATCH_SIZE);
	}

	@Override
	public String check(PaymentEntity[] payments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment_Batch.class;// 查询
	}

	@Override
	public String getBankTransCode() {
		return TRANS_CODE_KQ;
	}

	@Override
	public String getBankTransDesc() {
		return "快钱批量代扣";
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
		for (int i = 0; i < payments.length; i++) {
			String bankSeqId =payments[i].getSeqId();
			payments[i].setBankSeqId(String.valueOf(bankSeqId));
		}
	}

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
		// 获取前置机的配置信息
		KQFrontEndConfig kqConfig = (KQFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		KqAccountEntityExt  account = (KqAccountEntityExt)TransContext.getContext().getMainAccount();

		// 数据组装转换
		DealInfoEntity dealInfo = PaymentKqService.initBatchPayData(payments, kqConfig, account);
		CustomerTool ct = new CustomerTool();
		
		// 提交请求数据入口
		MerchantDebitPkiRequest request = ct.getMerchantDebitPkiRequest(dealInfo);
		String postContent = StringUtils.ReqFormat(CustomerUtil.merchantDebitPkiRequestToXml(request));
		return postContent;
	}

	@Override
	public void parse(String respStr, PaymentEntity[] payments)throws ParseMessageException {
		// 获取前置机的配置信息
		KQFrontEndConfig kqConfig = (KQFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		KqAccountEntityExt  account = (KqAccountEntityExt)TransContext.getContext().getMainAccount();
		
		
		// 解析返回数据
		String responseXML = StringUtils.ResFormat(respStr);
	    MerchantDebitPkiResponse response = CustomerUtil.xmlToMerchantDebitPkiResponse(responseXML);
	    
		DealInfoEntity dealInfo = PaymentKqService.initBatchPayData(payments, kqConfig, account);
		// 解密返回数据
		CustomerTool ct = new CustomerTool();
		MerchantDebitResponse bsar = (MerchantDebitResponse) ct.unseal(response, dealInfo);		
		
		/**
		 * 批次请求处理结果
		 * 00000   批次已接受
		 * 00001   批次校验失败
		 * 00002  批次完成
		 * 00003  批次处理中
		 */
		String failCode=bsar.getBatchErr();
		String failreason=bsar.getBatchErrMessage();
		String dealResult=bsar.getDealResult();
		if(dealResult.equals(KQConstants.BATCH_DEAL_RESULT_00000)){
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUBMITTED, "", bsar.getDealResult(), "提交成功");
		} else if(dealResult.equals(KQConstants.BATCH_DEAL_RESULT_00001)){
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
