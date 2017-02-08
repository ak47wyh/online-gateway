package com.iboxpay.settlement.gateway.common.dao;

import com.iboxpay.settlement.gateway.common.domain.SequenceEntity;
import com.iboxpay.settlement.gateway.common.domain.SequenceRange;

/**
 * 流水号
 * @author jianbo_chen
 */
public interface SequenceDao extends BaseDao<SequenceEntity> {

    /**
     * 得到下一个可用的seq范围
     * @param key : 流水号键值
     * @param range : 取号的范围.如100，即一次取100个
     * @return
     */
    public SequenceRange getSequenceRange(String key, int range);
}
