package com.iboxpay.settlement.gateway.common.trans.check;

import java.math.BigDecimal;
import java.util.Date;

import com.iboxpay.settlement.gateway.common.util.StringUtils;

public class CheckerData {

    private String customerAccNo;
    private String customerAccName;
    private BigDecimal amount;
    private Date transDate;
    private int status;
    private Long targetId;

    public CheckerData(String customerAccNo, String customerAccName, BigDecimal amount, Date transDate, Long targetId, int status) {
        this.customerAccNo = StringUtils.trim(customerAccNo);
        this.customerAccName = StringUtils.trim(customerAccName);
        this.amount = amount;
        this.transDate = transDate;
        this.status = status;
        this.targetId = targetId;
    }

    public CheckerData(String customerAccNo, String customerAccName, BigDecimal amount, Date transDate, Long targetId) {
        this(customerAccNo, customerAccName, amount, transDate, targetId, -1);
    }

    public String getCustomerAccName() {
        return customerAccName;
    }

    public String getCustomerAccNo() {
        return customerAccNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Date getTransDate() {
        return transDate;
    }

    public int getStatus() {
        return status;
    }

    public Long getTargetId() {
        return targetId;
    }
}