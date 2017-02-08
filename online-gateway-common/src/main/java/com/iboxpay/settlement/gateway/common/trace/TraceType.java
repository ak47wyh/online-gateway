package com.iboxpay.settlement.gateway.common.trace;

public enum TraceType {
    TRANS("业务处理"), //交易过程(包括网络，数据库等)
    NET("网络处理")//只是网络跟踪
    ;

    private String name;

    private TraceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
