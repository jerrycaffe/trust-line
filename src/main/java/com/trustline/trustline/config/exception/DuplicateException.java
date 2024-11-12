package com.trustline.trustline.config.exception;

public class DuplicateException extends AbstractException {

    public DuplicateException(String message) {
        this("D01", message);
    }

    public DuplicateException(String code, String message) {
        super(code, message);
    }
}
