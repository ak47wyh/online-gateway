package com.iboxpay.settlement.gateway.common.inout.refund;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 详细支付信息
 * @author jianbo_chen
 */
public class RefundPaymentCustomerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    //中间件使用的单笔流水号，同一批次内不能重复.【必须填写】
    private String seqId;
    //交易金额.【必须填写】
    private BigDecimal amount;
    //其他扩展属性，如身份证等
    private Map<String,Object> extProperties;

    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Map<String,Object> getExtProperties() {
        return extProperties;
    }

    public void setExtProperties(Map<String,Object> extProperties) {
        this.extProperties = extProperties;
    }
}
