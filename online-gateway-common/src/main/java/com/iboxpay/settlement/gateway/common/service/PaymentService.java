package com.iboxpay.settlement.gateway.common.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.cache.remote.MemcachedService;
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.domain.BatchPaymentEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;

@Service
public class PaymentService {

    @Resource
    private PaymentDao paymentDao;

    @Resource
    private MemcachedService memcachedService;

    /**
     * 从缓存或者数据库中读取支付对象
     * @param ids
     * @return
     */
    public PaymentEntity[] getPaymentEntitys(Long[] ids) {
        String[] strIds = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            strIds[i] = ids[i].toString();
        }
        PaymentEntity[] payments = new PaymentEntity[ids.length];
        Object[] objs = memcachedService.getWithType(strIds, PaymentEntity.class);
        java.util.Map<Long, Integer> notInCacheIds = new java.util.HashMap<Long, Integer>();
        boolean exist = false;
        int index = -1;

        if (objs != null) for (int i = 0; i < objs.length; i++) {
            if (objs[i] != null) {
                payments[i] = (PaymentEntity) objs[i];
                index = i;
            }
        }

        for (int j = 0; j < ids.length; j++) {
            exist = false;
            for (int i = 0; i <= index; i++) {
                if (payments[i] != null && ids[j].longValue() == payments[i].getId()) {
                    exist = true;
                    break;
                }
            }
            if (!exist) notInCacheIds.put(ids[j], j);
        }

        if (notInCacheIds.size() > 0) {
            List<PaymentEntity> list = paymentDao.getPaymentsByIds(notInCacheIds.keySet().toArray(new Long[0]));
            for (PaymentEntity paymentEntity : list) {
                payments[notInCacheIds.get(paymentEntity.getId())] = paymentEntity;
            }
        }
        return payments;
    }

    public void saveBatchPayment(BatchPaymentEntity batchPaymentEntity) {
        paymentDao.save(batchPaymentEntity);
        List<PaymentEntity> payments = batchPaymentEntity.getPaymentEntitys();
        PaymentEntity paymentEntity;
        for (int i = 0; i < payments.size(); i++) {
            paymentEntity = payments.get(i);
            memcachedService.setWithType(paymentEntity.getId().toString(), paymentEntity);
        }
    }

    public void update(PaymentEntity paymentEntity) {
        paymentDao.update(paymentEntity);
        memcachedService.setWithType(paymentEntity.getId().toString(), paymentEntity);
    }

    public void update(PaymentEntity paymentEntitys[]) {
        for (PaymentEntity paymentEntity : paymentEntitys)
            paymentDao.update(paymentEntity);

        for (PaymentEntity paymentEntity : paymentEntitys)
            memcachedService.setWithType(paymentEntity.getId().toString(), paymentEntity);
    }

    public void updateStatus(PaymentEntity paymentEntitys[], boolean isQuery) {
        paymentDao.updatePaymentStatus(paymentEntitys, isQuery);

        for (PaymentEntity paymentEntity : paymentEntitys)
            memcachedService.setWithType(paymentEntity.getId().toString(), paymentEntity);
    }

    /**
     * 为同步状态查询返回指定字段
     * @param batchSeqId
     * @param fieldNames
     * @return
     */
    public List<PaymentEntity> loadForQueryByBatchSeqId(String batchSeqId, String[] fieldNames) {
        StringBuilder sb = new StringBuilder("select ");
        if (fieldNames != null) {
            for (int i = 0; i < fieldNames.length; i++) {
                if (i > 0) sb.append(",");

                sb.append(fieldNames[i]);
            }
        }
        sb.append(" from PaymentEntity where batchSeqId=:batchSeqId");
        return paymentDao.findByHQL(sb.toString(), batchSeqId);
    }

    public void cache(List<PaymentEntity> payments) {
        if (payments != null && payments.size() > 0) {
            for (PaymentEntity paymentEntity : payments) {
                memcachedService.setWithType(paymentEntity.getId().toString(), paymentEntity);
            }
        }
    }
}
