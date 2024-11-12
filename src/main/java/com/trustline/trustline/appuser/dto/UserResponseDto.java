package com.trustline.trustline.appuser.dto;

import com.trustline.trustline.appuser.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
public class UserResponseDto {

    private UUID id;
    private  String email;
    private String phoneNumber;
    private boolean emailVerified;

    public static UserResponseDto fromUser(User user){
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .emailVerified(user.isAccountVerified())
                .build();
    }
}
