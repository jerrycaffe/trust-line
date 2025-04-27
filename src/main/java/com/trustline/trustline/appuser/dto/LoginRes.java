package com.trustline.trustline.appuser.dto;

import lombok.Data;

@Data
public class LoginRes<T> {
    private String accessToken;
    private String message;
    private T data;
}
