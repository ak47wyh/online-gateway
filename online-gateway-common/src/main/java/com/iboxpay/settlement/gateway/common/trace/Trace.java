package com.iboxpay.settlement.gateway.common.trace;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;

/**
 * 执行时间跟踪
 * @author jianbo_chen
 */
public class Trace {

    private static ThreadLocal<ThreadTraceInfo> threadTraceInfos = new ThreadLocal<ThreadTraceInfo>();
    private static ConcurrentHashMap<String, BankTraceInfo> bankTraceInfos = new ConcurrentHashMap<String, BankTraceInfo>();
    //一天重置一次
    private static long nextResetTime = DateTimeUtil.truncateTime(DateTimeUtil.addDay(new Date(), 1)).getTime();

    public synchronized static void reset() {
        bankTraceInfos = new ConcurrentHashMap<String, BankTraceInfo>();
    }

    private static void autoReset() {
        if (System.currentTimeMillis() > nextResetTime) {
            reset();
            nextResetTime = DateTimeUtil.truncateTime(DateTimeUtil.addDay(new Date(), 1)).getTime();
        }
    }

    public static void beginTrace() {
        autoReset();
        ThreadTraceInfo threadTraceInfo = new ThreadTraceInfo();
        threadTraceInfo.beginTrace(TraceType.TRANS);//总的跟踪交易
        threadTraceInfos.set(threadTraceInfo);
    }

    public static void endTrace() {
        ThreadTraceInfo threadTraceInfo = threadTraceInfos.get();
        threadTraceInfo.endTrace(TraceType.TRANS);
        TransContext context = TransContext.getContext();
        String bank = context.getBankProfile().getBankName();
        BankTraceInfo bankTraceInfo = bankTraceInfos.get(bank);
        if (bankTraceInfo == null) {
            bankTraceInfo = new BankTraceInfo(bank);
            BankTraceInfo oldBankTraceInfo = bankTraceInfos.putIfAbsent(bank, bankTraceInfo);
            if (oldBankTraceInfo != null) bankTraceInfo = oldBankTraceInfo;
        }
        bankTraceInfo.assembleTrace(context.getTransCode(), threadTraceInfo);
    }

    public static void beginTrace(TraceType traceType) {
        ThreadTraceInfo threadTraceInfo = threadTraceInfos.get();
        threadTraceInfo.beginTrace(traceType);
    }

    public static void endTrace(TraceType traceType) {
        ThreadTraceInfo threadTraceInfo = threadTraceInfos.get();
        threadTraceInfo.endTrace(traceType);
    }

    /**
     * 当前统计信息
     * @return
     */
    public static ConcurrentHashMap<String, BankTraceInfo> getBankTraceInfos() {
        return bankTraceInfos;
    }
}
