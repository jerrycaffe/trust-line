package com.trustline.trustline.appuser.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class OtpRequest {
    @Null(message = "verification id is required")
    private String verificationId;
    @NotNull(message = "phone number is required")
    private String phoneNumber;
}
