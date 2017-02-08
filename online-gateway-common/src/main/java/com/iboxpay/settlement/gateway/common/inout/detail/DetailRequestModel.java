package com.iboxpay.settlement.gateway.common.inout.detail;

import java.math.BigDecimal;

import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;

/**
 * 交易明细请求
 * @author jianbo_chen
 */
public class DetailRequestModel extends CommonRequestModel {

    private static final long serialVersionUID = 1L;

    //页大小
    private int pageSize;
    //第几页(1开始)
    private int pageNo = 1;
    private String customerAccNo;
    private String customerAccName;
    private BigDecimal beginAmount;
    private BigDecimal endAmount;
    private boolean queryCredit;

    //日期范围 yyyy-MM-dd
    private String beginDate;
    private String endDate;
    private boolean forceUpdate;//强制更新(到银行读取最新数据)

    //	{"accNo":"9999999999999999999",
    //		"beginDate":"2014-08-12","endDate":"2014-08-12",
    //		"customerAccNo":"","customerAccName":"",
    //		"beginAmount":"","endAmount":"","isQueryCredit":false}

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageNo() {
        return pageNo;
    }

    public String getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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

    public boolean isQueryCredit() {
        return queryCredit;
    }

    public void setQueryCredit(boolean queryCredit) {
        this.queryCredit = queryCredit;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}
