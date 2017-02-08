/**      
* PageUtil.java Create on 2011-10-18     
*      
* Copyright (c) 2011 iboxpay.com.  All rights reserved.
*         
* @version 1.0 
*     
*/
package com.iboxpay.settlement.gateway.common.page;

import java.util.regex.Pattern;

/**
 * @author weiyuanhua
 * @date 2012-7-4 下午3:18:13
 * 分页工具类
 */
public class PageUtil {

    /**
     * 转换页数
     * @param pageNoStr
     * @return
     */
    public static int getPageNo(String pageNoStr) {
        int pageNo = 0;
        if (pageNoStr == null || !isNumeric(pageNoStr)) {
            pageNo = 1;
        } else {
            pageNo = Integer.valueOf(pageNoStr);
        }
        return pageNo;
    }

    /**
     * 转换每页数量
     * @param pageSizeStr
     * @return
     */
    public static int getPageSize(String pageSizeStr) {
        int pageSize = PageBean.DEFAULT_PAGESIZE;

        if (pageSizeStr != null && isNumeric(pageSizeStr)) {
            pageSize = Integer.valueOf(pageSizeStr);
        }

        return pageSize;
    }

    /**
     * 判断是否为数字字符串
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

}
