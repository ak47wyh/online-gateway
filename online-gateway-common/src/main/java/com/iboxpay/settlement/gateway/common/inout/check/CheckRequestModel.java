package com.iboxpay.settlement.gateway.common.inout.check;

import java.math.BigDecimal;

import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;

public class CheckRequestModel extends CommonRequestModel {

    private static final long serialVersionUID = 1L;

    //页大小
    private int pageSize;
    //第几页(1开始)
    private int pageNo = 1;
    private String customerAccNo;
    private String customerAccName;
    private BigDecimal beginAmount;
    private BigDecimal endAmount;
    private int status;//交易状态
    //交易日期yyyy-MM-dd
    private String transDate;
    private String hasCheck;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
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

    public BigDecimal getBeginAmount() {
        return beginAmount;
    }

    public void setBeginAmount(BigDecimal beginAmount) {
        this.beginAmount = beginAmount;
    }

    public BigDecimal getEndAmount() {
        return endAmount;
    }

    public void setEndAmount(BigDecimal endAmount) {
        this.endAmount = endAmount;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public String getHasCheck() {
        return hasCheck;
    }

    public void setHasCheck(String hasCheck) {
        this.hasCheck = hasCheck;
    }

}
