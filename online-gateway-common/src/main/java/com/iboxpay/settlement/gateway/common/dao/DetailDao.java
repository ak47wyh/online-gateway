package com.iboxpay.settlement.gateway.common.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.iboxpay.settlement.gateway.common.domain.DetailEntity;
import com.iboxpay.settlement.gateway.common.domain.DetailQueryRecordEntity;
import com.iboxpay.settlement.gateway.common.page.PageBean;

public interface DetailDao extends BaseDao<DetailEntity> {

    /**
     * 更新/插入交易明细
     * @param detailEntitys
     * @param accountEntity
     * @param beginDate
     * @param endDate
     */
    public void saveDetails(DetailEntity detailEntitys[], String accNo, Date beginDate, Date endDate);

    /**
     * 从库中查询明细
     * @param accNo
     * @param beginDate
     * @param endDate
     * @return
     */
    public PageBean queryDetails(String accNo, String customerAccNo, String customerAccName, BigDecimal beginAmount, BigDecimal endAmount, boolean isQueryCredit, Date beginDate, Date endDate,
            int pageSize, int pageNo);

    /**
     * 更新查询历史记录
     * @param session
     * @param accNo
     * @param beginDate
     * @param endDate
     */
    public void updateQueryRecords(String accNo, Date beginDate, Date endDate, int type);

    /**
     * 某个账号的查询历史记录.用于计算需要真正发起交易的时间区间.
     * @param accNo
     * @param beginDate
     * @param endDate
     * @return
     */
    public DetailQueryRecordEntity[] getAccountQueryRecords(String accNo, Date beginDate, Date endDate);

    /**
     * 决定真正需要发起明细查询请求的时间范围.之前查过的，时间久了（时间设置）不需要再查了，未查过的日期才需要查.
     * @param accNo
     * @param beginDate
     * @param endDate
     * @return
     */
    public List<Date[]> determineActualQueryDate(String accNo, Date beginDate, Date endDate);

    /**
     * 获取当日明细查询记录
     * @param accNo
     * @return
     */
    public DetailQueryRecordEntity getTodayQueryRecord(String accNo);
    /**
     * 导出交易明细,查询某个账号某段交易日期内的所有记录
     * @param accNo
     * @param beginDate
     * @param endDate
     * @return List<DetailEntity>
     * add it by caolipeng at 2015-06-02
     */
    public List<DetailEntity> getDetailList(String accNo, Date beginDate, Date endDate);
}
