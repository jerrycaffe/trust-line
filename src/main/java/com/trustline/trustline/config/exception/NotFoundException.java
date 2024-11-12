package com.trustline.trustline.config.exception;


import static com.trustline.trustline.config.exception.ErrorCodes.NOT_FOUND_ERROR_CODE;

public class NotFoundException extends AbstractException {

    public NotFoundException(String message) {
        this(NOT_FOUND_ERROR_CODE, message);
    }

    public NotFoundException(String code, String message) {
        super(code, message);
    }
}
