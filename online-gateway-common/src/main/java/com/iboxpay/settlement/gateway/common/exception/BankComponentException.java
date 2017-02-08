package com.iboxpay.settlement.gateway.common.exception;

/**
 * 银行组件相关异常
 * @author jianbo_chen
 */
public class BankComponentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BankComponentException() {
        super();
    }

    public BankComponentException(String message) {
        super(message);
    }
}
