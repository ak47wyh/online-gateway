package com.iboxpay.settlement.gateway.common.trans.detail;

import java.util.HashMap;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.domain.DetailEntity;

/**
 * 查询结果。临时保存分页信息，直到查询结束
 * @author jianbo_chen
 */
public class DetailResult {

    private DetailEntity[] detailEntitys;
    private boolean hasNextPage;//是否有下一页
    private Map<String, Object> params = new HashMap<String, Object>();//分页信息，开发人员自己保存

    public DetailResult(DetailEntity[] detailEntitys, boolean hasNextPage) {
        this.detailEntitys = detailEntitys;
        this.hasNextPage = hasNextPage;
    }

    public DetailResult(DetailEntity[] detailEntitys) {
        this.detailEntitys = detailEntitys;
    }

    public DetailEntity[] getDetailEntitys() {
        return detailEntitys;
    }

    public DetailResult setDetailEntitys(DetailEntity[] detailEntitys) {
        this.detailEntitys = detailEntitys;
        return this;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public DetailResult setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
        return this;
    }

    public DetailResult setParam(String key, Object value) {
        params.put(key, value);
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * 即pageInfoMap
     * @param params
     */
    public void setParams(Map<String, Object> pageInfoMap) {
        this.params = pageInfoMap;
    }
}
