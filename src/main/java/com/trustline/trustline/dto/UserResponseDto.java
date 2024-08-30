package com.trustline.trustline.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserResponseDto {

    private UUID id;
    private  String username;

}
