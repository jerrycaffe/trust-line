package com.trustline.trustline.appuser.dto;

import com.trustline.trustline.appuser.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class CreateUserRes {
    private User user;
    private UUID otpId;
}
