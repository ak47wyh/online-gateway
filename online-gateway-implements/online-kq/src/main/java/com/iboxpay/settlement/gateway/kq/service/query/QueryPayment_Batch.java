package com.iboxpay.settlement.gateway.kq.service.query;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.bill99.schema.ddp.product.MerchantDebitPkiRequest;
import com.bill99.schema.ddp.product.MerchantDebitPkiResponse;
import com.bill99.schema.ddp.product.MerchantDebitQueryResponse;
import com.bill99.schema.ddp.product.MerchantDebitQueryResponseItem;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.query.AbstractQueryPayment;
import com.iboxpay.settlement.gateway.kq.KQFrontEndConfig;
import com.iboxpay.settlement.gateway.kq.KqAccountEntityExt;
import com.iboxpay.settlement.gateway.kq.entity.DealInfoEntity;
import com.iboxpay.settlement.gateway.kq.service.KQConstants;
import com.iboxpay.settlement.gateway.kq.service.PaymentKqService;
import com.iboxpay.settlement.gateway.kq.service.api.CustomerTool;
import com.iboxpay.settlement.gateway.kq.service.util.CustomerUtil;
import com.iboxpay.settlement.gateway.kq.service.util.StringUtils;

/**
 * 快钱支付批量代付查询交易结果
 * @author liaoxiongjian
 * @date 2015-09-29 10:25
 */
@Service
public class QueryPayment_Batch extends AbstractQueryPayment{
	private static Logger logger = LoggerFactory.getLogger(QueryPayment_Batch.class);
	private final static String TRANS_CODE_QUERY_KQ= "queryKQ";
	@Override
	public String getBankTransCode() {
		return TRANS_CODE_QUERY_KQ;
	}
	@Override
	public String getBankTransDesc() {
		return "快钱代扣交易明细查询";
	}

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
		// 获取前置机信息
		TransContext context = TransContext.getContext();
		KQFrontEndConfig kqConfig = (KQFrontEndConfig)context.getFrontEndConfig();
		KqAccountEntityExt account = (KqAccountEntityExt)context.getMainAccount();
		
		// 组装查询数据报文头
		DealInfoEntity dealInfo=PaymentKqService.initBatchQueryData(payments, kqConfig, account);
		
		// 提交请求数据入口
		CustomerTool ct = new CustomerTool();
		MerchantDebitPkiRequest request = ct.getMerchantDebitPkiRequest(dealInfo);
		String postContent = StringUtils.ReqFormat(CustomerUtil.merchantDebitPkiRequestToXml(request));
		
		return postContent;
	}
	
	@Override
	public void parse(String respStr, PaymentEntity[] payments)throws ParseMessageException {
		// 获取前置机信息
		TransContext context = TransContext.getContext();
		KQFrontEndConfig kqConfig = (KQFrontEndConfig)context.getFrontEndConfig();
		KqAccountEntityExt account = (KqAccountEntityExt)context.getMainAccount();
		
		// 解析返回数据
		String responseXML = StringUtils.ResFormat(respStr);
	    MerchantDebitPkiResponse response = CustomerUtil.xmlToMerchantDebitPkiResponse(responseXML);
		

		// 组装查询数据报文头
		DealInfoEntity dealInfo=PaymentKqService.initBatchQueryData(payments, kqConfig, account);
	    
		// 解密返回数据
	    CustomerTool ct = new CustomerTool();
		MerchantDebitQueryResponse bsar = (MerchantDebitQueryResponse) ct.unseal(response, dealInfo);

		logger.info("批次总笔数："+bsar.getTotalCount());
		logger.info("批次处理结果："+bsar.getBatchResult());
		
		
		List<MerchantDebitQueryResponseItem> list= bsar.getItems();
		for (MerchantDebitQueryResponseItem item : list) {
			String seqId= item.getSeqId();
			String errCode=item.getErrCode();
			String errMessage=item.getErrMessage();
			String dealResult= item.getDealResult();
			for (PaymentEntity paymentEntity : payments) {
				if(seqId.equals(paymentEntity.getBankSeqId())){
					if(dealResult.equals(KQConstants.PAYMENT_SATUS_01001)){
						 PaymentStatus.setStatus(paymentEntity, PaymentStatus.STATUS_SUCCESS, "", dealResult, "扣款成功");
						break;
					} else if(dealResult.equals(KQConstants.PAYMENT_SATUS_01002)||dealResult.equals(KQConstants.PAYMENT_SATUS_01004)){
						PaymentStatus.setStatus(paymentEntity, PaymentStatus.STATUS_FAIL, "", errCode, errMessage);
						break;
					}else {//其它情况通过查询接口确定
						PaymentStatus.setStatus(paymentEntity, PaymentStatus.STATUS_UNKNOWN, "", errCode, errMessage);
						break;
					}
				}
			}
			
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
		return ((KQFrontEndConfig)TransContext.getContext().getFrontEndConfig()).getUri().getVal();
	}

}
