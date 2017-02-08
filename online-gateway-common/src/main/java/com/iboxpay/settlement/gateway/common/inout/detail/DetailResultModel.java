package com.iboxpay.settlement.gateway.common.inout.detail;

import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;

public class DetailResultModel extends CommonResultModel {

    private static final long serialVersionUID = 1L;

    //明细更新日期
    private String detailUpdateTime;
    //日期范围 yyyy-MM-dd
    private String beginDate;

    private String endDate;
    //页大小
    private int pageSize;
    //第几页(1开始)
    private int pageNo;
    //结果总数
    private int totalCount;
    //总页数
    private int totalPages;
    private DetailModelInfo[] detailModelInfos;

    public void setDetailModelInfos(DetailModelInfo[] detailModelInfos) {
        this.detailModelInfos = detailModelInfos;
    }

    public DetailModelInfo[] getDetailModelInfos() {
        return detailModelInfos;
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

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
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

    public String getDetailUpdateTime() {
        return detailUpdateTime;
    }

    public void setDetailUpdateTime(String detailUpdateTime) {
        this.detailUpdateTime = detailUpdateTime;
    }

}
