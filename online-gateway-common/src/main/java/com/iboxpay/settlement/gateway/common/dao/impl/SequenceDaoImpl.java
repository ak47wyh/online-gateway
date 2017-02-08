package com.iboxpay.settlement.gateway.common.dao.impl;

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.dao.SequenceDao;
import com.iboxpay.settlement.gateway.common.domain.SequenceEntity;
import com.iboxpay.settlement.gateway.common.domain.SequenceRange;

@Component("sequenceDao")
public class SequenceDaoImpl extends BaseDaoImpl<SequenceEntity> implements SequenceDao {

    @Override
    public SequenceRange getSequenceRange(String key, int range) {
        if (range < 1) throw new IllegalArgumentException("编码错误：range(" + range + ")不能小于1");

        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Date now = new Date();
            SequenceRange sequenceRange = null;
            for (;;) {//CAS
                SequenceEntity entity = (SequenceEntity) session.get(getEntityClass(), key);
                if (entity == null) {
                    entity = new SequenceEntity();
                    entity.setKey(key);
                    entity.setSeq(range);
                    entity.setCreateTime(now);
                    entity.setUpdateTime(now);
                    save(entity);
                    sequenceRange = new SequenceRange(key, 1, range);
                    break;
                } else {
                    long oldSeq = entity.getSeq();
                    long newSeq = oldSeq + range;
                    Query q = session.createQuery("update SequenceEntity s " + "set s.seq = :newSeq, s.updateTime = :updateTime " + "where s.key = :key and s.seq = :oldSeq");
                    q.setLong("newSeq", newSeq);
                    q.setTimestamp("updateTime", now);
                    q.setString("key", key);
                    q.setLong("oldSeq", oldSeq);

                    if (1 == q.executeUpdate()) {
                        sequenceRange = new SequenceRange(key, oldSeq + 1, newSeq);
                        break;
                    }
                }
            }
            tx.commit();
            return sequenceRange;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

}
