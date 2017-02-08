package com.iboxpay.settlement.gateway.kq.entity;

import java.io.Serializable;

/**
 * 与单笔订单相关的数据
 * */
public class OrderInfoEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 商家订单号 */
	private String seqId;
	/** 约定业务 */
	private String usage;
	/** 银行代码 */
	private String bankId;
	/** 帐户类型 */
	private String accType;
	/** 开户机构名称 */
	private String openAccDept;
	/** 账户名 */
	private String bankAcctName;
	/** 账户号 */
	private String bankAcctId;
	/** 有效期 */
	private String expiredDate;
	/** 证件类型 */
	private String idType;
	/** 证件号码 */
	private String idCode;
	/** 币种 */
	private String curType;
	/** 金额 */
	private String amount;
	/** 备注 */
	private String remark;
	/** 用途*/
	private String note;

	public String getSeqId() {
		return seqId;
	}

	public void setSeqId(String seqId) {
		this.seqId = seqId;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getAccType() {
		return accType;
	}

	public void setAccType(String accType) {
		this.accType = accType;
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

	public String getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(String expiredDate) {
		this.expiredDate = expiredDate;
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

	public String getCurType() {
		return curType;
	}

	public void setCurType(String curType) {
		this.curType = curType;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
