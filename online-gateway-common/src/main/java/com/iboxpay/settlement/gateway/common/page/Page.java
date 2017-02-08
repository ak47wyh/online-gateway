/**
 * 
 */
package com.iboxpay.settlement.gateway.common.page;

import java.util.List;

/**
 * @author kingmanager
 * @title Page.java
 * @package com.iboxpay.core.page
 * @description 客户端分页查询数据
 * @update 2011-11-8 下午03:02:03
 * @version V1.0
 */
public class Page<E> {

    // 数据总数目
    private int totalNumber;
    // 开始查询位置
    private int startNumber;
    // 每页显示条数
    private int pageSize = 10;
    // 当前页数
    private int pageNumber;
    // 总页数
    private int totalPages;
    // 查询的结果
    private List<E> resultList;

    public Page(int start, int size) {
        this.startNumber = start;
        this.pageSize = size;
    }

    public int getTotalNumber() {
        return totalNumber;
    }

    public void setTotalNumber(int totalNumber) {
        this.totalNumber = totalNumber;
    }

    public int getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(int startNumber) {
        this.startNumber = startNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getTotalPages() {
        int pages;
        if (this.totalNumber == 0) {
            pages = 0;
        } else if (this.totalNumber != 0 && this.totalNumber % this.pageSize == 0) {
            pages = this.totalNumber / this.pageSize;
        } else {
            pages = 1 + this.totalNumber / this.pageSize;
        }

        this.setTotalPages(pages);

        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<E> getResultList() {
        return resultList;
    }

    public void setResultList(List<E> resultList) {
        this.resultList = resultList;
    }

}
