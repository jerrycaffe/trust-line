package com.trustline.trustline.appuser.dto;

import com.trustline.trustline.appuser.model.Gender;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class RegisterUserDto {

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^\\S+@\\S+\\.\\S+$", message = "Email should be like test@test.com")
    private String email;
    @Size(min = 4, message = "password should be more than 3 characters")
    private String password;
    @NotBlank(message = "Phone number field is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;
}
