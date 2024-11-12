package com.trustline.trustline.config.exception;

import java.util.Collections;
import java.util.Map;

public class RestClientException extends AbstractException {
    public static final String REST_CLIENT_ERROR_CODE = "101";

    Map additionalInfo;

    public RestClientException(String message) {
        this(REST_CLIENT_ERROR_CODE, message);
        this.additionalInfo = Collections.emptyMap();
    }

    public RestClientException(String message, Map additionalInfo) {
        this(REST_CLIENT_ERROR_CODE, message);
        this.additionalInfo = additionalInfo;
    }

    public RestClientException(String code, String message) {
        super(code, message);
        this.additionalInfo = Collections.emptyMap();
    }

    public Map getAdditionalInfo() {
        return additionalInfo;
    }
}
