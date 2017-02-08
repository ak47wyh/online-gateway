package com.iboxpay.settlement.gateway.common.trans;

/**
 * 业务拦截器，一般用于登陆或者签名
 * @author jianbo_chen
 */
public interface IBankTransInterceptor<T> {

    /**
     * 交易前处理，一般用于签名.当支付出错时，这个会直接导致交易失败
     * @param t
     */
    public void beforeTrans(T transObject) throws Exception;

    /**
     * 交易后处理，预留.当支付时，这里不要修改支付结果!!!!
     * @param t
     */
    public void afterTrans(T transObject) throws Exception;

}
