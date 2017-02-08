package com.iboxpay.settlement.gateway.common.exception;

/**
 * 前置机类异常，包括证书问题等等
 * @author jianbo_chen
 */
public class FrontEndException extends BaseTransException {

    private static final long serialVersionUID = 1L;

    public FrontEndException() {
        super();
    }

    public FrontEndException(String message) {
        super(message);
    }

    public FrontEndException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrontEndException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        if (super.getMessage() != null) {
            return super.getMessage();
        } else if (getCause() != null) {
            return getCause().getMessage();
        } else {
            return null;
        }

    }
}
