package com.iboxpay.settlement.gateway.kq.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.kq.KQFrontEndConfig;
import com.iboxpay.settlement.gateway.kq.KqAccountEntityExt;
import com.iboxpay.settlement.gateway.kq.entity.DealInfoEntity;
import com.iboxpay.settlement.gateway.kq.entity.OrderInfoEntity;

/**
 * 快钱代扣初始化数据服务类
 * @author liaoxiongjian
 * @date 2015-12-19 14:19
 */
public class PaymentKqService {

	/**
	 * 初始化单笔代扣数据
	 * 
	 * @param paymentEntity
	 *            支付数据
	 * @param kqConfig
	 *            前置机配置
	 * @param account
	 *            帐号信息
	 * @return
	 */
	public static DealInfoEntity initSinglePayData(PaymentEntity paymentEntity,KQFrontEndConfig kqConfig,KqAccountEntityExt account){
		
		DealInfoEntity dealInfo=new DealInfoEntity();
		/** 服务名*/
		dealInfo.setServiceType(kqConfig.getDebitSingleType().getVal());
		/** 加密方式*/
		dealInfo.setFeatureCode(kqConfig.getFeatureCode().getVal());
		/** 版本*/
		dealInfo.setVersion(kqConfig.getVersion().getVal());
		/** 字符集*/
		String charset= kqConfig.getCharset().getVal();
		charset= initCharset(charset);
		dealInfo.setInputCharset(charset);
		/** 会员编号*/
		dealInfo.setMemberCode(account.getMemberCode().getVal());
		/** 收款帐号*/
		dealInfo.setMerchantAcctId(account.getMerchantAcctId().getVal());
		/** 合同号*/
		dealInfo.setContractId(account.getContractId().getVal());
		/** 商家订单号*/
		dealInfo.setSeqId(paymentEntity.getSeqId());
		/** 扣款请求流水*/
		dealInfo.setReqSeqno(paymentEntity.getBankSeqId());
		/** 付款账户类型*/
		if(paymentEntity.getCustomerAccType()==1){//对公账户
			dealInfo.setAccType(KQConstants.ACCOUNT_TYPE_0101); 
		}else if(paymentEntity.getCustomerAccType()==2){//个人账户
			dealInfo.setAccType(KQConstants.ACCOUNT_TYPE_0201); 
		}else if(paymentEntity.getCustomerAccType()==3){//对私个人存折
			dealInfo.setAccType(KQConstants.ACCOUNT_TYPE_0204);
		}
		/** 银行代码*/
		String bankFullName=paymentEntity.getCustomerBankFullName();
		String bankId=KQBankNoHelper.convertBankNo(bankFullName);
		if(StringUtils.isNotBlank(bankId)){
			dealInfo.setBankId(bankId); 
		}
		/** 付款账户开户机构名*/
		dealInfo.setOpenAccDept(paymentEntity.getCustomerBankBranchName());
		/** 帐号名*/
		dealInfo.setBankAcctName(paymentEntity.getCustomerAccName()); 
		/** 账户号*/
		dealInfo.setBankAcctId(paymentEntity.getCustomerAccNo()); 
		/** 收款金额*/
		dealInfo.setAmount(paymentEntity.getAmount().toString()); 
		/** 证件类型*/
		String idType=(String) paymentEntity.getExtProperty(PaymentEntity.EXT_PROPERTY_CertType);
		idType=initIdType(idType);
		dealInfo.setIdType(idType); 
		/** 证件号码*/
		String idCode=(String) paymentEntity.getExtProperty(PaymentEntity.EXT_PROPERTY_CertNo);
		dealInfo.setIdCode(idCode); 
		/** 有效期*/
		dealInfo.setExpiredDate(""); 
		/** 约定业务*/
		dealInfo.setUsage("代扣");  
		/** 币种*/
		dealInfo.setCurType(kqConfig.getCur().getVal());  
		/** 回调连接*/
		dealInfo.setBgUrl("");
		/** 备注*/
		dealInfo.setRemark(paymentEntity.getRemark()); 
		/** 扩展参数1*/
		dealInfo.setExt1("");
		/** 扩展参数2*/
		dealInfo.setExt2("");
	
		return dealInfo;
	}
	
	
	/**
	 * 初始化单笔明细查询数据
	 * 
	 * @param paymentEntity
	 *            支付数据
	 * @param kqConfig
	 *            前置机配置
	 * @param account
	 *            帐号信息
	 * @return
	 */
	public static DealInfoEntity initSingleQueryData(PaymentEntity paymentEntity,KQFrontEndConfig kqConfig,KqAccountEntityExt account){
		DealInfoEntity dealInfo=new DealInfoEntity();		
		
		/** 服务名*/
		dealInfo.setServiceType(kqConfig.getDebitbatchQueryType().getVal());
		/** 加密方式*/
		dealInfo.setFeatureCode(kqConfig.getFeatureCode().getVal());
		/** 版本*/
		dealInfo.setVersion(kqConfig.getQueryVersion().getVal());
		/** 字符集*/
		String charset= kqConfig.getCharset().getVal();
		charset= initCharset(charset);
		dealInfo.setInputCharset(charset);
		/** 会员编号*/
		dealInfo.setMemberCode(account.getMemberCode().getVal());
		/** 收款帐号*/
		dealInfo.setMerchantAcctId(account.getMerchantAcctId().getVal());
		/** 批次号*/
		dealInfo.setSeqId(paymentEntity.getBankBatchSeqId());
		dealInfo.setReqSeqno(paymentEntity.getBankSeqId());
		/** 收款帐号*/
		dealInfo.setRequestTime(DateTimeUtil.format(paymentEntity.getCreateTime(), "yyyyMMddHHmmss"));
		dealInfo.setExt1("");
		dealInfo.setExt2("");
		
		return dealInfo;
	}
	
	/**
	 * 初始化批量代扣支付
	 * 
	 * @param payments
	 *            支付数据
	 * @param kqConfig
	 *            前置机
	 * @param account
	 *            帐号信息
	 * @return
	 */
	public static DealInfoEntity initBatchPayData(PaymentEntity[] payments,KQFrontEndConfig kqConfig,KqAccountEntityExt account){
		DealInfoEntity dealInfo=new DealInfoEntity();
		List<OrderInfoEntity> itemList=new ArrayList<OrderInfoEntity>();
		BigDecimal totalAmts =new BigDecimal("0");
		for (PaymentEntity paymentEntity : payments) {
			BigDecimal detailAmt=paymentEntity.getAmount();
			totalAmts=totalAmts.add(detailAmt);
			
			OrderInfoEntity order=new OrderInfoEntity();
			/** 商家订单号*/
			order.setSeqId(paymentEntity.getBankSeqId());
			/** 约定业务*/
			order.setUsage("代扣");
			/** 银行代码*/
			String bankFullName=paymentEntity.getCustomerBankFullName();
			String bankId=KQBankNoHelper.convertBankNo(bankFullName);
			if(StringUtils.isNotBlank(bankId)){
				order.setBankId(bankId); 
			}
			/** 账户类型*/
			if(paymentEntity.getCustomerAccType()==1){//对公账户
				order.setAccType(KQConstants.ACCOUNT_TYPE_0101); 
			}else if(paymentEntity.getCustomerAccType()==2){//个人账户
				order.setAccType(KQConstants.ACCOUNT_TYPE_0201); 
			}else if(paymentEntity.getCustomerAccType()==3){//对私个人存折
				order.setAccType(KQConstants.ACCOUNT_TYPE_0204);
			}
			/** 开户机构名*/
			order.setOpenAccDept(paymentEntity.getCustomerBankBranchName());
			/** 账户名*/
			order.setBankAcctName(paymentEntity.getCustomerAccName());
			/** 帐户号*/
			order.setBankAcctId(paymentEntity.getCustomerAccNo());
			/** 金额*/
			order.setAmount(String.valueOf(paymentEntity.getAmount()));
			/** 有效期*/
			order.setExpiredDate("");
			/** 证件类型*/
			String idType=(String) paymentEntity.getExtProperty(PaymentEntity.EXT_PROPERTY_CertType);
			idType=initIdType(idType);
			order.setIdType(idType); 
			/** 证件号码*/
			String idCode=(String) paymentEntity.getExtProperty(PaymentEntity.EXT_PROPERTY_CertNo);
			order.setIdCode(idCode); 
			/** 币种*/
			order.setCurType(kqConfig.getCur().getVal());
			/** 用途*/
			order.setNote("");
			/** 备注*/
			order.setRemark(paymentEntity.getRemark());
			itemList.add(order);
		}		
		dealInfo.setItemList(itemList);
		/** 版本*/
		dealInfo.setVersion(kqConfig.getVersion().getVal());
		/** 加密方式*/
		dealInfo.setFeatureCode(kqConfig.getFeatureCode().getVal());
		/** 服务名*/
		dealInfo.setServiceType(kqConfig.getDebitbatchType().getVal());
		/** 字符集*/
		String charset= kqConfig.getCharset().getVal();
		charset= initCharset(charset);
		dealInfo.setInputCharset(charset);
		/** 会员编号*/
		dealInfo.setMemberCode(account.getMemberCode().getVal());
		/** 收款帐号*/
		dealInfo.setMerchantAcctId(account.getMerchantAcctId().getVal());
		/** 合同号*/
		dealInfo.setContractId(account.getContractId().getVal());
		dealInfo.setNumTotal(String.valueOf(payments.length));
		dealInfo.setRequestId(payments[0].getBankBatchSeqId());
		dealInfo.setRequestTime(DateTimeUtil.format(payments[0].getCreateTime(), "yyyyMMddHHmmss"));
		dealInfo.setAmountTotal(totalAmts.toString());
		dealInfo.setBgUrl("");
		dealInfo.setExt1("");
		dealInfo.setExt2("");
		return dealInfo;
	}
	
	/**
	 * 初始化批量代扣查询明细数据
	 * 
	 * @param payments
	 *            支付数据
	 * @param kqConfig
	 *            前置机
	 * @param account
	 *            帐号信息
	 * @return
	 */
	public static DealInfoEntity initBatchQueryData(PaymentEntity[] payments,KQFrontEndConfig kqConfig,KqAccountEntityExt account){
		DealInfoEntity dealInfo=new DealInfoEntity();
		/** 服务名*/
		dealInfo.setServiceType(kqConfig.getDebitbatchQueryType().getVal());
		/** 加密方式*/
		dealInfo.setFeatureCode(kqConfig.getFeatureCode().getVal());
		/** 版本*/
		dealInfo.setVersion(kqConfig.getQueryVersion().getVal());
		/** 字符集*/
		String charset= kqConfig.getCharset().getVal();
		charset= initCharset(charset);
		dealInfo.setInputCharset(charset);
		/** 会员编号*/
		dealInfo.setMemberCode(account.getMemberCode().getVal());
		/** 收款帐号*/
		dealInfo.setMerchantAcctId(account.getMerchantAcctId().getVal());
		/** 批次号*/
		dealInfo.setBatchId(payments[0].getBankBatchSeqId());
		/** 收款帐号*/
		dealInfo.setRequestTime(DateTimeUtil.format(new Date(), "yyyyMMddHHmmss"));
		dealInfo.setExt1("");
		dealInfo.setExt2("");
		return dealInfo;
	}

	
	/**
	 * 查询交易明细报文
	 * 
	 * @param kqConfig
	 *            前置机
	 * @param account
	 *            账号信息
	 * @param beginDate
	 *            开始日期
	 * @param endDate
	 *            结束日期
	 * @param pageInfoMap
	 *            分页信息
	 * @return
	 */
	public static DealInfoEntity initComplexQueryData(KQFrontEndConfig kqConfig,KqAccountEntityExt account,Date beginDate, Date endDate,Map<String, Object> pageInfoMap){
		DealInfoEntity dealInfo=new DealInfoEntity();
		/** 服务名*/
		dealInfo.setServiceType(kqConfig.getDebitbatchQueryType().getVal());
		/** 加密方式*/
		dealInfo.setFeatureCode(kqConfig.getFeatureCode().getVal());
		/** 版本*/
		dealInfo.setVersion(kqConfig.getQueryVersion().getVal());
		/** 字符集*/
		String charset= kqConfig.getCharset().getVal();
		charset= initCharset(charset);
		dealInfo.setInputCharset(charset);
		/** 会员编号*/
		dealInfo.setMemberCode(account.getMemberCode().getVal());
		/** 收款帐号*/
		dealInfo.setMerchantAcctId(account.getMerchantAcctId().getVal());
		/** 起始申请时间*/
		String beginApplyDate=DateTimeUtil.getTimeMillisY(beginDate,"yyyyMMdd")+"000000";
		dealInfo.setStartTime(beginApplyDate);
		/** 结束申请时间*/
		String endApplyDate=DateTimeUtil.getTimeMillisY(endDate,"yyyyMMdd")+"235959";
		dealInfo.setEndTime(endApplyDate);
		/** 页码*/
		String pageNo="";
		if(pageInfoMap.containsKey("pageNo")){
			pageNo=String.valueOf(pageInfoMap.get("pageNo"));
		}else{
			pageNo="1";
		}
		dealInfo.setPageNo(pageNo);
		/** 页码显示条数*/
		String pageSize = kqConfig.getPageSize().getVal();
		dealInfo.setPageSize(pageSize);
		/** 请求时间*/
		dealInfo.setRequestTime(DateTimeUtil.format(new Date(), "yyyyMMddHHmmss"));
		dealInfo.setExt1("");
		dealInfo.setExt2("");
		return dealInfo;
	}
	
	/**
	 * 字符编码转换
	 * @param charset
	 */
	private static String initCharset(String charset) {
		if(charset.equalsIgnoreCase(KQConstants.CHARSET_UTF8)){
			return KQConstants.KQ_CHARSET_UTF8;
		}else if(charset.equalsIgnoreCase(KQConstants.CHARSET_GBK)){
			return KQConstants.KQ_CHARSET_GBK;
		}else if(charset.equalsIgnoreCase(KQConstants.CHARSET_GB2312)){
			return KQConstants.KQ_CHARSET_GB2312;
		}
		return null;
	}
	
	
	
	/**
	 * 证件类型转换
	 * @param idType
	 * @return
	 */
	private static String initIdType(String idType){
		if (!StringUtils.isEmpty(idType)) {
			if (idType.equals("0")) {
				return KQConstants.ID_TYPE_101;
			} else if (idType.equals("1")) {
				return KQConstants.ID_TYPE_201;
			} else if (idType.equals("3")) {
				return KQConstants.ID_TYPE_105;
			} else if (idType.equals("4")) {
				return KQConstants.ID_TYPE_103;
			} else if (idType.equals("5")) {
				return KQConstants.ID_TYPE_102;
			} else if (idType.equals("6")) {
				return KQConstants.ID_TYPE_201;
			} else if (idType.equals("7")) {
				return KQConstants.ID_TYPE_107;
			} else if (idType.equals("8")) {
				return KQConstants.ID_TYPE_108;
			} else if (idType.equals("E")) {
				return KQConstants.ID_TYPE_104;
			} else if (idType.equals("Z")) {
				return KQConstants.ID_TYPE_900;
			} else {
				return KQConstants.ID_TYPE_101;//设置如果没有匹配到设置默认为身份证类型
			}
		} else{
			return KQConstants.ID_TYPE_101;//设置如果没有匹配到设置默认为身份证类型
		}
	}
}
