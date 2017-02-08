package com.iboxpay.settlement.gateway.common.trace;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.iboxpay.settlement.gateway.common.trans.TransCode;

/**
 * 银行业务时间统计信息
 * @author jianbo_chen
 */
public class BankTraceInfo {

    private String bank;
    private ConcurrentHashMap<TransCode, TimeTraceInfo> traceInfos = new ConcurrentHashMap<TransCode, TimeTraceInfo>();

    public BankTraceInfo(String bank) {
        this.bank = bank;
    }

    /**
     * 各线程把数据汇总进来
     */
    public void assembleTrace(TransCode tranCode, ThreadTraceInfo traceInfo) {
        Iterator<Map.Entry<TraceType, Long>> itr = traceInfo.getTotalTimeMap().entrySet().iterator();
        while (itr.hasNext()) {
            Entry<TraceType, Long> entry = itr.next();
            TimeTraceInfo timeTraceInfo = traceInfos.get(tranCode);
            if (timeTraceInfo == null) {
                timeTraceInfo = new TimeTraceInfo();
                TimeTraceInfo oldTimeTraceInfo = traceInfos.putIfAbsent(tranCode, timeTraceInfo);
                if (oldTimeTraceInfo != null) timeTraceInfo = oldTimeTraceInfo;
            }
            //时间加到总时间里
            AtomicLong totalTime = timeTraceInfo.totalTime.get(entry.getKey());
            if (totalTime == null) {
                totalTime = new AtomicLong(0);
                AtomicLong oldTotalTime = timeTraceInfo.totalTime.putIfAbsent(entry.getKey(), totalTime);
                if (oldTotalTime != null) totalTime = oldTotalTime;
            }
            if (entry.getValue() != null && entry.getValue() > 0L) {//执行过程有可能中断，如网络，如果中断就没有必要统计次数了，否则平均没有意义
                totalTime.addAndGet(entry.getValue());
                //总次数加1
                AtomicInteger totalTimes = timeTraceInfo.totalTimes.get(entry.getKey());
                if (totalTimes == null) {
                    totalTimes = new AtomicInteger(0);
                    AtomicInteger oldTotalTimes = timeTraceInfo.totalTimes.putIfAbsent(entry.getKey(), totalTimes);
                    if (oldTotalTimes != null) totalTimes = oldTotalTimes;
                }
                totalTimes.incrementAndGet();
            }
        }
    }

    public static class TimeTraceInfo {

        public final ConcurrentHashMap<TraceType, AtomicLong> totalTime = new ConcurrentHashMap<TraceType, AtomicLong>();//访问总时间
        public final ConcurrentHashMap<TraceType, AtomicInteger> totalTimes = new ConcurrentHashMap<TraceType, AtomicInteger>();//访问次数

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            TraceType[] traceTypes = TraceType.values();
            for (TraceType traceType : traceTypes) {
                AtomicLong _totalTime = totalTime.get(traceType);
                AtomicInteger _totalTimes = totalTimes.get(traceType);

                if (_totalTime == null || _totalTimes == null) continue;

                sb.append(traceType.getName()).append(" : ");
                sb.append("{总时间=").append(_totalTime).append("(ms)").append(", 总次数=").append(_totalTimes).append(", 平均时间=")
                        .append(_totalTimes.intValue() == 0 ? "N/A" : (_totalTime.longValue() / _totalTimes.intValue() + "(ms)")).append("}").append("\r\n");
            }
            return sb.toString();
        }
    }

    public String getBank() {
        return bank;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("===========").append(bank).append("============").append("\r\n");
        Iterator<Entry<TransCode, TimeTraceInfo>> itr = traceInfos.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<TransCode, TimeTraceInfo> entry = itr.next();
            sb.append(":::").append(entry.getKey()).append(":::").append("\r\n");
            sb.append(entry.getValue());
        }
        return sb.toString();
    }
}
