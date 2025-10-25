package com.trustline.trustline.appuser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordReq {
    @NotBlank(message = "newPassword field is expected")
    private String newPassword;
    @NotBlank(message = "userName field is required")
    private String userName;
}
