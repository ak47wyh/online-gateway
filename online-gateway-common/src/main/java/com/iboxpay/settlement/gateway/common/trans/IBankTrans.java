package com.iboxpay.settlement.gateway.common.trans;

/**
 * 银业务行具体实现
 * @author jianbo_chen
 */
public interface IBankTrans<T> {

    /**
     * 业务编码，如支付为pay
     * @return
     */
    public TransCode getTransCode();

    /**
     * 返回银行交易码(哪个接口).一旦确定了，不要随意改动，特别是在系统运行期，改动可能会导致某些业务无法正常进行(会写到数据库中).
     * @return
     */
    public String getBankTransCode();

    /**
     * 返回银行交易接口描述
     * @return
     */
    public String getBankTransDesc();
}
