package com.iboxpay.settlement.gateway.common.inout.balance;

import java.math.BigDecimal;

import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;

/**
 * 余额查询结果
 * @author jianbo_chen
 */
public class BalanceResultModel extends CommonResultModel {

    private static final long serialVersionUID = 1L;

    private String bankName;//银行名称
    private BigDecimal balance;//即时余额
    private BigDecimal availableBalance;//可用余额
    private String currency;//币别

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
