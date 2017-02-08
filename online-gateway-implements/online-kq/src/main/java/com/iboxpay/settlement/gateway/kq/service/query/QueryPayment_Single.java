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
 * 快钱代扣-免签约单笔查询
 * @author liaoxiongjian
 * @date 2015-12-14 10:25
 */
@Service
public class QueryPayment_Single extends AbstractQueryPayment{
	private static Logger logger = LoggerFactory.getLogger(QueryPayment_Batch.class);
	private final static String TRANS_CODE_QUERY_SINGLE_KQ= "querySingleKQ";
	@Override
	public String getBankTransCode() {
		return TRANS_CODE_QUERY_SINGLE_KQ;
	}

	@Override
	public String getBankTransDesc() {
		return "单笔代扣查询";
	}

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
		// 获取前置机的配置信息
		TransContext context = TransContext.getContext();
		KQFrontEndConfig kqConfig = (KQFrontEndConfig) context.getFrontEndConfig();
		KqAccountEntityExt  account = (KqAccountEntityExt)context.getMainAccount();
	
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		DealInfoEntity dealInfo=PaymentKqService.initSingleQueryData(paymentEntity, kqConfig, account);
		
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
		KqAccountEntityExt  account = (KqAccountEntityExt)context.getMainAccount();
		
		// 解析返回数据
		String responseXML = StringUtils.ResFormat(respStr);
		// 把xml转换为MerchantDebitPkiResponse
		MerchantDebitPkiResponse response = CustomerUtil.xmlToMerchantDebitPkiResponse(responseXML);

		PaymentEntity paymentEntity=payments[0];
		DealInfoEntity dealInfo=PaymentKqService.initSingleQueryData(paymentEntity, kqConfig, account);
		// 解密返回数据
		CustomerTool ct = new CustomerTool();
		MerchantDebitQueryResponse bsar = (MerchantDebitQueryResponse) ct.unseal(response, dealInfo);
		
		
		List<MerchantDebitQueryResponseItem> list = bsar.getItems();
		if (list != null && list.size() > 0) {
			MerchantDebitQueryResponseItem item = list.get(0);
			String seqId= item.getSeqId();
			String errCode=item.getErrCode();
			String errMessage=item.getErrMessage();
			String dealResult= item.getDealResult();
	        if(payments[0].getBankBatchSeqId().equals(seqId)){
	        	if ( dealResult.equals(KQConstants.PAYMENT_SATUS_01001)) {
	    			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "",dealResult, "扣款成功");
	    		} else if (dealResult.equals(KQConstants.PAYMENT_SATUS_01002) || dealResult.equals(KQConstants.PAYMENT_SATUS_01004)) {
	    			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "",errCode, errMessage);
	    		} else {// 其它情况通过查询接口确定
	    			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "",errCode, errMessage);
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
