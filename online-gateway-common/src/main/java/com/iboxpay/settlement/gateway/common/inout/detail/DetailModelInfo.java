package com.iboxpay.settlement.gateway.common.inout.detail;

import java.math.BigDecimal;
import java.util.Date;

public class DetailModelInfo {

    private long id;// long
    private String accNo;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String currency;
    private String transDate;//yyyy-MM-dd HH:mm:ss
    private String remark;
    private String useCode;
    private String useDesc;
    private BigDecimal balance;
    private String customerAccNo;
    private String customerAccName;
    private String customerBankFullName;
    private String bankBatchSeqId;
    private String createTime;
    private String updateTime;

    // --预留字段(JSON串)
    private String reserved;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public String getTransDate() {
        return transDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUseCode() {
        return useCode;
    }

    public void setUseCode(String useCode) {
        this.useCode = useCode;
    }

    public String getUseDesc() {
        return useDesc;
    }

    public void setUseDesc(String useDesc) {
        this.useDesc = useDesc;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public void setBankBatchSeqId(String bankBatchSeqId) {
        this.bankBatchSeqId = bankBatchSeqId;
    }

    public String getBankBatchSeqId() {
        return bankBatchSeqId;
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
}
