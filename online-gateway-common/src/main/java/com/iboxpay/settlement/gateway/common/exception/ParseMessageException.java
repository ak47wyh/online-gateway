package com.iboxpay.settlement.gateway.common.exception;

public class ParseMessageException extends BaseTransException {

    private static final long serialVersionUID = 1L;

    public ParseMessageException() {
        super();
    }

    public ParseMessageException(String message) {
        super(message);
    }

    public ParseMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseMessageException(Throwable cause) {
        super(cause);
    }
}
