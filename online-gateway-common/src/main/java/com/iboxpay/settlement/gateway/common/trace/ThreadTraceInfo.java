package com.iboxpay.settlement.gateway.common.trace;

import java.util.HashMap;
import java.util.Map;

public class ThreadTraceInfo {

    private Map<TraceType, Long> visitBeginTimeMap = new HashMap<TraceType, Long>();
    private Map<TraceType, Long> totalTimeMap = new HashMap<TraceType, Long>();//单线程访问

    public void beginTrace(TraceType type) {
        visitBeginTimeMap.put(type, System.currentTimeMillis());
    }

    public void endTrace(TraceType type) {
        Long beginTime = visitBeginTimeMap.get(type);
        if (beginTime == null || beginTime.longValue() == 0L) //防止不正确关闭连接导致错误
            return;

        long last = System.currentTimeMillis() - beginTime;
        totalTimeMap.put(type, last);
    }

    public Map<TraceType, Long> getTotalTimeMap() {
        return totalTimeMap;
    }
}
