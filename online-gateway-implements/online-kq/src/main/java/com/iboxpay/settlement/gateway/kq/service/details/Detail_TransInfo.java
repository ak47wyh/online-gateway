package com.iboxpay.settlement.gateway.kq.service.details;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bill99.schema.ddp.product.MerchantDebitPkiRequest;
import com.bill99.schema.ddp.product.MerchantDebitPkiResponse;
import com.bill99.schema.ddp.product.MerchantDebitQueryResponse;
import com.bill99.schema.ddp.product.MerchantDebitQueryResponseItem;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.dao.CommonSessionFactory;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.DetailEntity;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.detail.AbstractDetail;
import com.iboxpay.settlement.gateway.common.trans.detail.DetailResult;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.kq.KQFrontEndConfig;
import com.iboxpay.settlement.gateway.kq.KqAccountEntityExt;
import com.iboxpay.settlement.gateway.kq.entity.DealInfoEntity;
import com.iboxpay.settlement.gateway.kq.service.KQConstants;
import com.iboxpay.settlement.gateway.kq.service.PaymentKqService;
import com.iboxpay.settlement.gateway.kq.service.api.CustomerTool;
import com.iboxpay.settlement.gateway.kq.service.util.CustomerUtil;
import com.iboxpay.settlement.gateway.kq.service.util.StringUtils;

/**
 * 快钱代扣查询交易明细
 * @author liaoxiongjian
 * @date 2016-1-9 16:18
 */
@Service
public class Detail_TransInfo extends AbstractDetail{
	private static Logger logger = LoggerFactory.getLogger(Detail_TransInfo.class);
	private final static String BANK_TRANS_CODE_GetTransInfo = "GetTransInfo";
	@Override
	public int supportQueryHisDaysSpan() {
		// TODO Auto-generated method stub
		return 30;
	}

	@Override
	public String getBankTransCode() {
		 return BANK_TRANS_CODE_GetTransInfo;
	}

	@Override
	public String getBankTransDesc() {
		return "交易明细查询";
	}

	@Override
	public String packHisDetail(AccountEntity accountEntity, Date beginDate, Date endDate,Map<String, Object> pageInfoMap) throws PackMessageException {
		// 获取前置机信息
		TransContext context = TransContext.getContext();
		KQFrontEndConfig kqConfig = (KQFrontEndConfig)context.getFrontEndConfig();
		KqAccountEntityExt account = (KqAccountEntityExt)context.getMainAccount();
		
		// 组装查询数据报文头
		DealInfoEntity dealInfo=PaymentKqService.initComplexQueryData(kqConfig, account,beginDate,endDate,pageInfoMap);
		
		// 提交请求数据入口
		CustomerTool ct = new CustomerTool();
		MerchantDebitPkiRequest request = ct.getMerchantDebitPkiRequest(dealInfo);
		String postContent = StringUtils.ReqFormat(CustomerUtil.merchantDebitPkiRequestToXml(request));
		
		return postContent;
	}

	@Override
	public DetailResult parseHisDetail(AccountEntity accountEntity, Date beginDate, Date endDate, String respStr,
			Map<String, Object> pageInfoMap) throws ParseMessageException {
		// 获取前置机信息
		TransContext context = TransContext.getContext();
		KQFrontEndConfig kqConfig = (KQFrontEndConfig) context.getFrontEndConfig();
		KqAccountEntityExt account = (KqAccountEntityExt) context.getMainAccount();

		// 解析返回数据
		String responseXML = StringUtils.ResFormat(respStr);
		MerchantDebitPkiResponse response = CustomerUtil.xmlToMerchantDebitPkiResponse(responseXML);

		// 组装查询数据报文头
		DealInfoEntity dealInfo = PaymentKqService.initComplexQueryData(kqConfig, account, beginDate, endDate,pageInfoMap);

		// 解密返回数据
		CustomerTool ct = new CustomerTool();
		MerchantDebitQueryResponse bsar = (MerchantDebitQueryResponse) ct.unseal(response, dealInfo);
		
		List<DetailEntity> detailList = new LinkedList<DetailEntity>();
		logger.info("批次总笔数：" + bsar.getTotalCount());
		logger.info("批次处理结果：" + bsar.getBatchResult());
		List<Map<String, Object>> paymentList = readPaymentEntitys(accountEntity.getAccNo(), beginDate);

		List<MerchantDebitQueryResponseItem> list = bsar.getItems();
		for (MerchantDebitQueryResponseItem item : list) {
			String seqId = item.getSeqId();
			String errCode = item.getErrCode();
			String errMessage = item.getErrMessage();
			String dealResult = item.getDealResult();

		
			//匹配流水号
			Map<String, Object> paymentMap=findPaymentByOrderId(paymentList,seqId);			
			BigDecimal detailAmt=new BigDecimal(item.getAmount());
			BigDecimal balanceAmt=new BigDecimal(0);
			if(com.iboxpay.settlement.gateway.common.util.StringUtils.isNotBlank(item.getBalance())){
			    balanceAmt=new BigDecimal(item.getBalance());
			}
			if(paymentMap!=null){
				DetailEntity detail = new DetailEntity();
				detail.setCurrency(kqConfig.getCur().getVal());				
				detail.setDebitAmount(BigDecimal.ZERO);
				detail.setCreditAmount(detailAmt);
				detail.setBalance(balanceAmt);
				detail.setCustomerAccNo(String.valueOf(paymentMap.get("customerAccNo")));
				detail.setCustomerAccName(String.valueOf(paymentMap.get("customerAccName")));
				detail.setCustomerBankFullName(String.valueOf(paymentMap.get("customerBankFullName")));
				
				String remark="";
				if(dealResult.equals(KQConstants.PAYMENT_SATUS_01001)){
					remark="扣款成功"; 
				} else if(dealResult.equals(KQConstants.PAYMENT_SATUS_01002)||dealResult.equals(KQConstants.PAYMENT_SATUS_01004)){
					remark="扣款失败【错误代码】"+ errCode+"【错误原因】"+errMessage; 
				}else {//其它情况通过查询接口确定
					remark="交易进行中【错误代码】"+errCode+"【错误原因】"+errMessage; 
				}
				detail.setRemark(remark);
				// 用途
				detail.setUseCode("");
				detail.setUseDesc("代扣");
				detail.setTransDate(DateTimeUtil.parseDate(String.valueOf(paymentMap.get("transDate")), "yyyy-MM-dd HH:mm:ss"));
				detail.setBankBatchSeqId(String.valueOf(paymentMap.get("bankSeqId")));//订单号
				detail.setReserved(dealResult);//预留字段：订单处理状态
				detailList.add(detail);
			}
		}
		
		/**
		 * 分页查询快钱商户申请信息
		 * 备注：因为第一次默认执行1次，后续循环采用N-1的次数遍历
		 */
		DetailResult result=null;
		if(detailList!=null&&detailList.size()>0){
			int totalPage= Integer.parseInt(bsar.getTotalPage());//总页数        
	        int loopNum=1;
	        if(pageInfoMap.containsKey("pageNo")){
	        	loopNum=Integer.parseInt(pageInfoMap.get("pageNo").toString());
	        }
	        if(totalPage>loopNum){
	        	result = new DetailResult(detailList.toArray(new DetailEntity[0]), true);
	            pageInfoMap.put("pageNo", ++loopNum);//设置下一页查询页码
	            result.setParams(pageInfoMap);
	        }else if(totalPage==loopNum){
	        	result = new DetailResult(detailList.toArray(new DetailEntity[0]), false);
	        }
		}
		return result;
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
	
	
	/**
     * 根据查询日期获取交易数据
     * @param accNo
     * @param session
     * @param beginTransDate
     * @param endTransDate
     * @return
	 * @throws ParseMessageException 
     */
    private List<Map<String, Object>> readPaymentEntitys(String accNo, Date beginTransDate) throws ParseMessageException {
		/*结束申请时间*/
        Date endTransDate = DateTimeUtil.addDay(beginTransDate, 1);
    	
    	List<Map<String, Object>> paymentList=null;
    	Session session=null;
		try {
		    session = CommonSessionFactory.getHibernateSession();
			Query q =session.createQuery(" select " 
		                               + " new map(p.id as id, p.amount as amount, p.bankBatchSeqId as bankBatchSeqId,p.bankSeqId as bankSeqId, p.status as status, "
					                   + " p.submitPayTime as transDate,p.customerAccNo as customerAccNo,p.customerAccName as customerAccName,"
					                   + " p.customerBankFullName as customerBankFullName,p.remark as remark,p.transDate as transDate ) "
			                           + " from PaymentEntity p " 
					                   + " where p.accNo=:accNo " 
			                           + " and p.transDate>=:beginTransDate " 
					                   + " and p.transDate<:endTransDate " 
			                           + " and p.status >= :status "
			                           + " order by p.bankBatchSeqId asc");//对可能存在一天内一个收款账号出现多次情况
			q.setString("accNo", accNo);
			q.setDate("beginTransDate", beginTransDate);
			q.setDate("endTransDate", endTransDate);
			q.setInteger("status", PaymentStatus.STATUS_SUBMITTED);
			paymentList = q.list();
		} catch (HibernateException e) {
			logger.error("根据查询日期获取交易数据发生异常",e);
		} finally{
			if (session.isOpen()) session.close();
		}
        return paymentList;
    }
    
    /**
     * 根据返回的商家订单号查找交易信息
     * @param paymentList 付款集合
     * @param orderId 订单号
     * @return
     */
	private Map<String, Object> findPaymentByOrderId(List<Map<String, Object>> paymentList, String orderId) {
		for (Map<String, Object> payment : paymentList) {
			String bankBatchSeqId = (String) payment.get("bankBatchSeqId");
			if (bankBatchSeqId.equals(orderId)) {
				return payment;
			}
		}
		return null;
	}

}
