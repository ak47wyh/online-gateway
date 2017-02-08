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

//余额表,预留，可用于查询历史余额
@Entity
@Table(name = "T_EB_AccBalance")
public class BalanceEnity implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "balance_generator")
    @SequenceGenerator(name = "balance_generator", sequenceName = "BALANCE_ID_SEQ", allocationSize = 20, initialValue = 1)
    @Column(name = "ID")
    private Integer id;//long

    @Column(name = "BANK_NAME")
    private String bankName;//银行名称

    @Column(name = "ACC_NO")
    private String accNo;//	VARCHAR2(30)	交易主账号

    @Column(name = "BALANCE")
    private BigDecimal balance;//即时余额//	NUMBER(19,2)	付款金额

    @Column(name = "AVAILABLE_BALANCE")
    private BigDecimal availableBalance;//可用余额//	NUMBER(19,2)	付款金额

    @Column(name = "CURRENCY")
    private String currency;//	char(5)	币别

    @Column(name = "CREATE_TIME")
    private Date createTime;//	TIMESTAMP(6)	

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

}
