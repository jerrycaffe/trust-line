package com.trustline.trustline.appuser.controller;


import com.trustline.trustline.appuser.dto.*;
import com.trustline.trustline.appuser.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Validated RegisterUserDto registerUserDto) {
        return UserResponseDto.fromUser(userService.createUser(registerUserDto));
    }

    @GetMapping("/verify-otp")
    public String verifyOtp(
            @Validated @RequestBody OtpRequest otpRequest
    ) {
        return userService.verifyOtp(otpRequest);
    }


    @PostMapping("/login")
    public LoginRes<UserResponseDto> login(@Validated @RequestBody LoginReq loginReq) {
        return userService.login(loginReq);
    }

}
