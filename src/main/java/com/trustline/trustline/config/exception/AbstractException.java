package com.trustline.trustline.config.exception;


public class AbstractException extends RuntimeException {
    String code;
    Error error;

    public AbstractException(String code, String message) {
        super(message);
        this.code = code;
        this.error = new Error(code, message);
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
