package com.iboxpay.settlement.gateway.common.domain;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jianbo_chen
 */
public class SequenceRange {

    public final String key;
    private AtomicLong currentSeq;
    public final long start;//包含
    public final long end;

    public SequenceRange(String key, long start, long end) {
        this.key = key;
        this.start = start;
        this.end = end;
        this.currentSeq = new AtomicLong(start);
    }

    /**
     * 获取下一个可用的流水
     * @return
     */
    public long nextSeq() {
        AtomicLong currentSeq = this.currentSeq;
        while (true) {
            long current = currentSeq.get();

            if (current > end) //已被取完了
                return -1;

            long next = current + 1;
            if (currentSeq.compareAndSet(current, next)) return next;
        }
    }

}
