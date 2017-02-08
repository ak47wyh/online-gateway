package com.iboxpay.settlement.gateway.common.inout.payment;

import java.io.Serializable;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.inout.CommonExtProperties;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;

public class PaymentCustomerResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private String seqId;//	单笔流水号 中间件使用的单笔流水号
	private String accNo;//客户账号
	private String accName;//客户账户名
	private String status;//付款状态：未提交 给银行   银行处理中   交易成功   交易失败//  交易异常
	private String statusMsg;//银企中间件提供的付款状态描述信息

	private String bankBatchSeqId;//银行批次号
	private String bankSeqId;//银行明细批次号

	private String payErrorCode;
	private String payBankStatus;
	private String payBankStatusMsg;

	private String errorCode;//错误码
	private String bankStatus;//银行提供的付款单状态码
	private String bankStatusMsg;//银行提供的付款状态描述信息
	private Map<String,Object> extProperties;

	public String getSeqId() {
		return seqId;
	}

	public void setSeqId(String seqId) {
		this.seqId = seqId;
	}

	public String getAccNo() {
		return accNo;
	}

	public void setAccNo(String accNo) {
		this.accNo = accNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode == null ? null : errorCode.name();
	}

	public String getBankBatchSeqId() {
		return bankBatchSeqId;
	}

	public void setBankBatchSeqId(String bankBatchSeqId) {
		this.bankBatchSeqId = bankBatchSeqId;
	}

	public String getBankSeqId() {
		return bankSeqId;
	}

	public void setBankSeqId(String bankSeqId) {
		this.bankSeqId = bankSeqId;
	}

	public String getBankStatus() {
		return bankStatus;
	}

	public void setBankStatus(String bankStatus) {
		this.bankStatus = bankStatus;
	}

	public String getBankStatusMsg() {
		return bankStatusMsg;
	}

	public void setBankStatusMsg(String bankStatusMsg) {
		this.bankStatusMsg = bankStatusMsg;
	}

	public String getPayBankStatus() {
		return payBankStatus;
	}

	public void setPayBankStatus(String payBankStatus) {
		this.payBankStatus = payBankStatus;
	}

	public String getPayBankStatusMsg() {
		return payBankStatusMsg;
	}

	public void setPayBankStatusMsg(String payBankStatusMsg) {
		this.payBankStatusMsg = payBankStatusMsg;
	}

	public String getPayErrorCode() {
		return payErrorCode;
	}

	public void setPayErrorCode(ErrorCode payErrorCode) {
		this.payErrorCode = payErrorCode == null ? null : payErrorCode.name();
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public void setAccName(String accName) {
		this.accName = accName;
	}

	public String getAccName() {
		return accName;
	}

	public Map<String,Object> getExtProperties() {
		return extProperties;
	}

	public void setExtProperties(Map<String,Object> extProperties) {
		this.extProperties = extProperties;
	}

}
