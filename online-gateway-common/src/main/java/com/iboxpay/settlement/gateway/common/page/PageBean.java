/**      
* PageBean.java Create on 2011-10-18     
*      
* Copyright (c) 2011 iboxpay.com.  All rights reserved.
*         
* @version 1.0 
*     
*/
package com.iboxpay.settlement.gateway.common.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * @author weiyuanhua
 * @date 2012-7-4 下午3:17:27
 * 分页封装对象
 */
public class PageBean {

    private static Logger log = LoggerFactory.getLogger(PageBean.class);

    public static void main(String[] args) {
        log.trace("======trace");
        log.debug("======debug");
        log.info("======info");
        log.warn("======warn");
        log.error("======error");
    }

    public static final int DEFAULT_PAGESIZE = 10;
    /**
     * 每页数量
     */
    private int pageSize = DEFAULT_PAGESIZE;
    /**
     * 开始位置
     */
    private int startIndex;
    /**
     * 总数
     */
    private long totalCount;
    /**
     * 当前页数
     */
    private int pageNo;
    /**
     * 总页数
     */
    private int totalPages;
    /**
     * 分页的结果
     */
    private List result;

    /**
     * @param pageNo
     * @param pageSize
     * @param totalCount
     */
    public PageBean(int pageNo, int pageSize, long totalCount) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return the startIndex
     */
    public int getStartIndex() {
        if (this.getPageNo() <= 0) {
            this.setStartIndex(0);
        } else if (this.getPageNo() > this.getTotalPages()) {
            this.setStartIndex((this.getTotalPages() - 1) * this.getPageSize());
        } else {
            this.setStartIndex((this.getPageNo() - 1) * this.getPageSize());
        }
        return this.startIndex;
    }

    /**
     * @param startIndex the startIndex to set
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * @return the pageNo
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * @param pageNo the pageNo to set
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * @return the totalPages
     */
    public int getTotalPages() {
        int pages = (int) (this.getTotalCount() / this.getPageSize());
        if (this.getTotalCount() % this.getPageSize() == 0 && this.getTotalCount() != 0) {
            this.setTotalPages(pages);
        } else {
            this.setTotalPages(pages + 1);
        }
        return this.totalPages;
    }

    /**
     * @param totalPages the totalPages to set
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * @return the result
     */
    public List getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(List result) {
        this.result = result;
    }

    /**
     * @return the totalCount
     */
    public long getTotalCount() {
        return totalCount;
    }

    /**
     * @param totalCount the totalCount to set
     */
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isPrevious() {
        if (this.getPageNo() <= 1 || this.getPageNo() > this.getTotalPages()) {
            return false;
        } else {
            return true;
        }

    }

    public boolean isNext() {
        if (this.getPageNo() < 1 || this.getPageNo() >= this.getTotalPages()) {
            return false;
        } else {
            return true;
        }

    }

}
