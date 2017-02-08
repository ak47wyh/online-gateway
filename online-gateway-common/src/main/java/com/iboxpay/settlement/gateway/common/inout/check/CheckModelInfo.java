package com.iboxpay.settlement.gateway.common.inout.check;

import java.math.BigDecimal;

public class CheckModelInfo {

    private String batchSeqId;
    private String seqId;
    private Long paymentId;
    private String accNo;
    private BigDecimal amount;
    //实际上为提交银行时间
    private String transTime;//yyyy-MM-dd HH:mm:ss
    private String customerAccNo;
    private String customerAccName;
    private String customerBankFullName;

    private Long detailId;
    private int status;
    private String statusMsg;
    private int checkStatus;
    private String checkStatusMsg;

    private String createTime;
    private String updateTime;

    public void setBatchSeqId(String batchSeqId) {
        this.batchSeqId = batchSeqId;
    }

    public String getBatchSeqId() {
        return batchSeqId;
    }

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getCustomerAccNo() {
        return customerAccNo;
    }

    public void setCustomerAccNo(String customerAccNo) {
        this.customerAccNo = customerAccNo;
    }

    public String getCustomerAccName() {
        return customerAccName;
    }

    public void setCustomerAccName(String customerAccName) {
        this.customerAccName = customerAccName;
    }

    public String getCustomerBankFullName() {
        return customerBankFullName;
    }

    public void setCustomerBankFullName(String customerBankFullName) {
        this.customerBankFullName = customerBankFullName;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }

    public String getCheckStatusMsg() {
        return checkStatusMsg;
    }

    public void setCheckStatusMsg(String checkStatusMsg) {
        this.checkStatusMsg = checkStatusMsg;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

}
