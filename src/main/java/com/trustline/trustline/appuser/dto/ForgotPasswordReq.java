package com.trustline.trustline.appuser.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForgotPasswordReq {
    @NotNull(message = "email is required")
    private String email;
}
