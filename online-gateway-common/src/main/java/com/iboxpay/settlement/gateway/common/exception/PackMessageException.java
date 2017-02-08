package com.iboxpay.settlement.gateway.common.exception;

public class PackMessageException extends BaseTransException {

    private static final long serialVersionUID = 1L;

    public PackMessageException() {
        super();
    }

    public PackMessageException(String message) {
        super(message);
    }

    public PackMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public PackMessageException(Throwable cause) {
        super(cause);
    }
}
