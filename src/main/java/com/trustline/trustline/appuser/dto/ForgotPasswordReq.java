package com.trustline.trustline.appuser.dto;

import lombok.Data;

@Data
public class ForgotPasswordReq {
    private String email;
    private String phoneNumber;
}
