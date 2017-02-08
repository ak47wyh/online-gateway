package com.iboxpay.settlement.gateway.common.inout.payment;

import java.io.Serializable;
import java.util.Date;

import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;

/**
 * 支付信息
 * @author jianbo_chen
 */
public class PaymentRequestModel extends CommonRequestModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String batchSeqId;//中间件使用的批量流水号
    //ISO币别.可空，默认为CNY人民币
    private String currency;
    
    /**优先级（默认为0(最低)， 优先级分为32个(0 - 32)，最高优先级应设置为32*/
    private int priority = DEFAULT_PRIORITY;
    public final static int DEFAULT_PRIORITY = 0;
    public final static int HIGH_PRIORITY = 32;
    
    //预约日期	指定交易日期
    @Deprecated
    private Date transDate;
    //预约交易日	1 为T+1。transDate不为空时，优先使用处理。
    @Deprecated
    private int transDay = -1;
    
    private PaymentCustomerInfo[] data;//	支付信息 支持批量支付

    public String getBatchSeqId() {
        return batchSeqId;
    }

    public void setBatchSeqId(String batchSeqId) {
        this.batchSeqId = batchSeqId;
    }

    public PaymentCustomerInfo[] getData() {
        return data;
    }

    public void setData(PaymentCustomerInfo[] data) {
        this.data = data;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getTransDate() {
        return transDate;
    }

    public void setTransDate(Date transDate) {
        this.transDate = transDate;
    }

    public int getTransDay() {
        return transDay;
    }

    public void setTransDay(int transDay) {
        this.transDay = transDay;
    }

    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        if(priority > HIGH_PRIORITY)
            priority = HIGH_PRIORITY;
        else if(priority < DEFAULT_PRIORITY)
            priority = DEFAULT_PRIORITY;
        
        this.priority = priority;
    }

}
