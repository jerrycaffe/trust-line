package com.trustline.trustline.config.exception;


public class BadRequestException extends AbstractException {

    public BadRequestException(String message) {
        this(ErrorCodes.FORMAT_ERROR_CODE, message);
    }

    public BadRequestException(String code, String message) {
        super(code, message);
    }
}
