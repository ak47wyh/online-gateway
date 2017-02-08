package com.iboxpay.settlement.gateway.kq.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 与批次交易相关的数据
 * */
public class DealInfoEntity implements Serializable {
	/**
	 * 自动增长ID
	 */
	private static final long serialVersionUID = 316185246251023757L;

	/** 版本号 */
	private String version = "1.0";
	/** 提交类型 */
	private String serviceType;
	/** 商户编号 */
	private String memberCode;
	/** 加密类型 */
	private String featureCode;
	/** 字符编码 */
	private String inputCharset;
	/** 收款帐号 */
	private String merchantAcctId;
	/** 合同号 */
	private String contractId;
	/** 商家订单号 */
	private String seqId;
	/** 扣款流水 */
	private String reqSeqno;
	/** 付款账户类型 */
	private String accType;
	/** 银行代码 */
	private String bankId;
	/** 付款账户开户机构名 */
	private String openAccDept;
	/** 付款方银行账户 */
	private String bankAcctName;
	/** 付款方账号 */
	private String bankAcctId;
	/** 收款金额 */
	private String amount;
	/** 证件类型 */
	private String idType;
	/** 证件号码 */
	private String idCode;
	/** 有效期 */
	private String expiredDate;
	/** 约定业务 */
	private String usage;
	/** 币种 */
	private String curType;
	/** 回调地址*/
	private String bgUrl;
	/** 备注 */
	private String remark;
	/** 预留字段1 */
	private String ext1;
	/** 预留字段2 */
	private String ext2;

	/** 扣款请求流水号reqSeqno,必填 */
	private String merchantSeqNo;
	/** 交易代码 */
	private String transcode;
	/** 批次号 */
	private String batchId;
	/** 请求批次号 */
	private String requestId;
	/** 请求时间 */
	private String requestTime;
	/** 总笔数 */
	private String numTotal;
	/** 总金额 */
	private String amountTotal;
	/** 开始时间*/
	private String startTime;
	/** 结束时间*/
	private String endTime;
	/** 页码*/
	private String pageNo;
	/** 页码笔数*/
	private String pageSize;
	/** 明细列表*/
	private List<OrderInfoEntity> itemList;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getMemberCode() {
		return memberCode;
	}

	public void setMemberCode(String memberCode) {
		this.memberCode = memberCode;
	}

	public String getFeatureCode() {
		return featureCode;
	}

	public void setFeatureCode(String featureCode) {
		this.featureCode = featureCode;
	}

	public String getInputCharset() {
		return inputCharset;
	}

	public void setInputCharset(String inputCharset) {
		this.inputCharset = inputCharset;
	}

	public String getMerchantAcctId() {
		return merchantAcctId;
	}

	public void setMerchantAcctId(String merchantAcctId) {
		this.merchantAcctId = merchantAcctId;
	}

	public String getContractId() {
		return contractId;
	}

	public void setContractId(String contractId) {
		this.contractId = contractId;
	}

	public String getSeqId() {
		return seqId;
	}

	public void setSeqId(String seqId) {
		this.seqId = seqId;
	}

	public String getReqSeqno() {
		return reqSeqno;
	}

	public void setReqSeqno(String reqSeqno) {
		this.reqSeqno = reqSeqno;
	}

	public String getAccType() {
		return accType;
	}

	public void setAccType(String accType) {
		this.accType = accType;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getOpenAccDept() {
		return openAccDept;
	}

	public void setOpenAccDept(String openAccDept) {
		this.openAccDept = openAccDept;
	}

	public String getBankAcctName() {
		return bankAcctName;
	}

	public void setBankAcctName(String bankAcctName) {
		this.bankAcctName = bankAcctName;
	}

	public String getBankAcctId() {
		return bankAcctId;
	}

	public void setBankAcctId(String bankAcctId) {
		this.bankAcctId = bankAcctId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
	}

	public String getIdCode() {
		return idCode;
	}

	public void setIdCode(String idCode) {
		this.idCode = idCode;
	}

	public String getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(String expiredDate) {
		this.expiredDate = expiredDate;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getCurType() {
		return curType;
	}

	public void setCurType(String curType) {
		this.curType = curType;
	}

	public String getBgUrl() {
		return bgUrl;
	}

	public void setBgUrl(String bgUrl) {
		this.bgUrl = bgUrl;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getExt1() {
		return ext1;
	}

	public void setExt1(String ext1) {
		this.ext1 = ext1;
	}

	public String getExt2() {
		return ext2;
	}

	public void setExt2(String ext2) {
		this.ext2 = ext2;
	}

	public String getMerchantSeqNo() {
		return merchantSeqNo;
	}

	public void setMerchantSeqNo(String merchantSeqNo) {
		this.merchantSeqNo = merchantSeqNo;
	}

	public String getTranscode() {
		return transcode;
	}

	public void setTranscode(String transcode) {
		this.transcode = transcode;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}

	public String getNumTotal() {
		return numTotal;
	}

	public void setNumTotal(String numTotal) {
		this.numTotal = numTotal;
	}

	public String getAmountTotal() {
		return amountTotal;
	}

	public void setAmountTotal(String amountTotal) {
		this.amountTotal = amountTotal;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getPageNo() {
		return pageNo;
	}

	public void setPageNo(String pageNo) {
		this.pageNo = pageNo;
	}

	public String getPageSize() {
		return pageSize;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public List<OrderInfoEntity> getItemList() {
		return itemList;
	}

	public void setItemList(List<OrderInfoEntity> itemList) {
		this.itemList = itemList;
	}

}
