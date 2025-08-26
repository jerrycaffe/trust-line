package com.trustline.trustline.appuser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OtpVerificationResponse {
    private String message;
    private UUID otpId;
}
