package com.trustline.trustline.config.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCodes {
    public static final String SERVER_ERROR_CODE = "06";
    public static final String SERVER_ERROR_MESSAGE = "General Error.";
    public static final String DUPLICATE_ERROR_CODE = "94";
    public static final String DUPLICATE_ERROR_MESSAGE = "Duplicate Record.";
    public static final String FORMAT_ERROR_CODE = "30";
    public static final String FORMAT_ERROR_MESSAGE = "Format Error.";
    public static final String NOT_FOUND_ERROR_CODE = "25";
    public static final String NOT_FOUND_ERROR_MESSAGE = "Not Found.";

    public static final String ILLEGAL_INPUT_ERROR_CODE = "76";
    public static final String ILLEGAL_INPUT_ERROR_MESSAGE = "Improper Argument passed";
}
