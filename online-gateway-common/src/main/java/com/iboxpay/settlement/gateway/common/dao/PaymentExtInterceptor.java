package com.iboxpay.settlement.gateway.common.dao;

import org.hibernate.EmptyInterceptor;

/**
 * 用于支持支付分表。先按不同银行来分，看业务情况再作其他分法.
 * @author jianbo_chen
 */
public class PaymentExtInterceptor extends EmptyInterceptor {

    private static final long serialVersionUID = 1L;

}
