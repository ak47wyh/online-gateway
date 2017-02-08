package com.iboxpay.settlement.gateway.common.inout.check;

import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;

public class CheckResultModel extends CommonResultModel {

    private static final long serialVersionUID = 1L;

    //明细更新日期
    private String detailUpdateTime;
    //日期 yyyy-MM-dd
    private String transDate;
    //页大小
    private int pageSize;
    //第几页(1开始)
    private int pageNo;
    //结果总数
    private int totalCount;
    //总页数
    private int totalPages;

    private CheckModelInfo checkModelInfos[];

    public String getDetailUpdateTime() {
        return detailUpdateTime;
    }

    public void setDetailUpdateTime(String detailUpdateTime) {
        this.detailUpdateTime = detailUpdateTime;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

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

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setCheckModelInfos(CheckModelInfo[] checkModelInfos) {
        this.checkModelInfos = checkModelInfos;
    }

    public CheckModelInfo[] getCheckModelInfos() {
        return checkModelInfos;
    }
}
