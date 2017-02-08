package com.iboxpay.settlement.gateway.common.trans.detail;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

/**
 * 交易明细查询接口
 * @author jianbo_chen
 */
public interface IDetail extends IBankTrans {

    /**
     * 查询当日明细
     * @param accountEntity : 查询账号
     * @param pageInfoMap : 分页参数信息.第一页时为空
     * @return
     * @throws BaseTransException
     * @throws IOException
     */
    public DetailResult queryTodayDetail(AccountEntity accountEntity, int pageIndex, Map<String, Object> pageInfoMap) throws BaseTransException;

    /**
     * 查询历史明细
     * @param accountEntity : 查询账号
     * @param beginDate : 开始日期
     * @param endDate : 结束日期
     * @param 
     * @return
     * @throws BaseTransException
     * @throws IOException
     */
    public DetailResult queryHisDetail(AccountEntity accountEntity, Date beginDate, Date endDate, int pageIndex, Map<String, Object> pageInfoMap) throws BaseTransException;

    /**
     * 当日明细是否是独立的接口.如果返回true,框架在调用时才会调用{@link #queryTodayDetail(AccountEntity accountEntity)}
     * @return
     */
    public boolean isTodayDetailIndependent();

    /**
     * 支持查询历史明细的天数跨度.系统会根据这个值进行日期的自动拆分，分别发送请求
     * @return
     */
    public int supportQueryHisDaysSpan();
}
