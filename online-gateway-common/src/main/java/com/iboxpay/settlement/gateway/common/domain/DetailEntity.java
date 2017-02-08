package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.iboxpay.settlement.gateway.common.util.ClassUtil;

@Entity
@Table(name = "T_EB_Detail")
public class DetailEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    //		  	ID int PRIMARY KEY,
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detail_generator")
    @SequenceGenerator(name = "detail_generator", sequenceName = "DETAIL_ID_SEQ", allocationSize = 50, initialValue = 1)
    @Column(name = "ID")
    private long id;//long

    //		  	BANK_NAME  VARCHAR2(10),  --银行简码  
    @Column(name = "BANK_NAME")
    private String bankName;

    //		  	ACC_NO  VARCHAR2(30) not null,  --交易主账号
    @Column(name = "ACC_NO")
    private String accNo;

    //		  	ACC_NAME  NVARCHAR2(30),  --交易主账户名
    @Column(name = "ACC_NAME")
    private String accName;

    //			DEBIT_AMOUNT	NUMBER(19,2),--借
    @Column(name = "DEBIT_AMOUNT")
    private BigDecimal debitAmount = BigDecimal.ZERO;

    //			CREDIT_AMOUNT	NUMBER(19,2),--贷
    @Column(name = "CREDIT_AMOUNT")
    private BigDecimal creditAmount = BigDecimal.ZERO;

    //		  	CURRENCY  VARCHAR2(5),  --  交易币别
    @Column(name = "CURRENCY")
    private String currency;

    //			TRANS_DATE	TIMESTAMP(6), --交易日期
    @Column(name = "TRANS_DATE")
    private Date transDate;

    //			ORDER_INDEX	NUMBER(10), --查询时返回顺序号，作为排序依据(如果交易时间只有天)
    @Column(name = "ORDER_INDEX")
    private int orderIndex;

    //			REMARK	NVARCHAR2(255),	--备注
    @Column(name = "REMARK")
    private String remark;

    //		  	USE_CODE  NVARCHAR2(50),  --  用途代码
    @Column(name = "USE_CODE")
    private String useCode;

    //		  	USE_DESC  NVARCHAR2(255),  --  用途描述
    @Column(name = "USE_DESC")
    private String useDesc;

    //			BALANCE	NUMBER(19,2), --余额		
    @Column(name = "BALANCE")
    private BigDecimal balance;

    //		 	CUSTOMER_ACC_NO  VARCHAR2(30),  --客户账号
    @Column(name = "CUSTOMER_ACC_NO")
    private String customerAccNo;

    //		  	CUSTOMER_ACC_NAME  NVARCHAR2(30),  --  客户账户名
    @Column(name = "CUSTOMER_ACC_NAME")
    private String customerAccName;

    //		  	CUSTOMER_BANK_FULL_NAME NVARCHAR2(30),  -- 客户银行全称
    @Column(name = "CUSTOMER_BANK_FULL_NAME")
    private String customerBankFullName;

    @Column(name = "BANK_BATCH_SEQ_ID")
    private String bankBatchSeqId;

    //			RESERVED	NVARCHAR2(1000),--预留字段(JSON串)
    @Column(name = "RESERVED")
    private String reserved;

    @Column(name = "FIELDS_HASH")
    private String fieldsHash;//关键字段的hash值，用于快束找到旧的记录

    //			CREATE_TIME  TIMESTAMP(6),  --创建时间
    @Column(name = "CREATE_TIME")
    private Date createTime;//	TIMESTAMP(6)	N

    //		  	UPDATE_TIME  TIMESTAMP(6)  --更新时间 
    @Column(name = "UPDATE_TIME")
    private Date updateTime;//	TIMESTAMP(6)	N	

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
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

    public Date getTransDate() {
        return transDate;
    }

    public void setTransDate(Date transDate) {
        this.transDate = transDate;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getBankBatchSeqId() {
        return bankBatchSeqId;
    }

    public void setBankBatchSeqId(String bankBatchSeqId) {
        this.bankBatchSeqId = bankBatchSeqId;
    }

    public String getFieldsHash() {
        return fieldsHash;
    }

    public void setFieldsHash(String fieldsHash) {
        this.fieldsHash = fieldsHash;
    }

    @Override
    public String toString() {
        return ClassUtil.toString(this);
    }
}
