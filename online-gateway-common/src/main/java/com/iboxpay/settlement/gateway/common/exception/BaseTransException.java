package com.iboxpay.settlement.gateway.common.exception;

public class BaseTransException extends Exception {

    private static final long serialVersionUID = 1L;

    public BaseTransException() {
        super();
    }

    public BaseTransException(String message) {
        super(message);
    }

    public BaseTransException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseTransException(Throwable cause) {
        super(cause);
    }
}
