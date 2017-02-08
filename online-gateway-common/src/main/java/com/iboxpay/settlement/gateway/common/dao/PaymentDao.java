package com.iboxpay.settlement.gateway.common.dao;

import java.util.Date;
import java.util.List;

import com.iboxpay.settlement.gateway.common.domain.BatchPaymentEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;

public interface PaymentDao extends BaseDao<PaymentEntity> {

    public List<PaymentEntity> getPaymentsByIds(Long[] ids);

    //	public PaymentEntity getBySettleId(String settleId);
    /**
     * CAS 准备提交，以免并发重复支付
     */
    public PaymentEntity[] prepareSubmitting(PaymentEntity[] paymentEntitys);

    public void save(BatchPaymentEntity batchEntity);

    /**
     * 根据批次号读取
     * @param batchSeqId 批次号
     * @param lazy 是否延迟读取支付对象
     * @return
     */
    public BatchPaymentEntity getBatchPaymentEntity(String batchSeqId);
    
    /**
     * 根据银行批次流水查询付款信息
     * @param bankSeqId 银行批次号
     * @return
     */
    public PaymentEntity getPaymentsByBankSeqId(String bankSeqId);

    /**
     * 更新同步次数
     * @param id
     */
    public void updateQueryTransCount(Long id);

    /**
     * 批量更新
     * @param paymentEntitys
     */
    public void updateBatch(PaymentEntity paymentEntitys[]);

    /**
     * 查找某一种状态的支付信息
     * @param status
     * @return
     */
    public List findPaymentByStatus(int status);

    /**
     * 更新状态,为了避免hibernate默认的全字段update
     * @param paymentEntitys
     */
    public void updatePaymentStatus(PaymentEntity paymentEntitys[], boolean isQuery);
    /**
     * 根据批次号、账号、查询日期得到对应的List<PaymentEntity>
     * @param batchSeqId
     * @param accNo
     * @param beginDate
     * @param endDate
     * @return List<PaymentEntity>
     * add it by caolipeng at 2015-08-18
     */
    public List<String> findPaymentByBatchSeqId(List<String> batchSeqIds,String accNo,Date beginDate,Date endDate);
    /**
     * 根据账号、查询日期得到对应的 List<String>
     * @param accNo
     * @param beginDate
     * @param endDate   
     * @return List<String> batchSeqId集合
     * add it by caolipeng at 2015-08-18
     */
    public List<String> findBatchSeqIdByAccAndDate(String accNo,Date beginDate,Date endDate);
}
