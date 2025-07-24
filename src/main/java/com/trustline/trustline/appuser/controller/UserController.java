package com.trustline.trustline.appuser.controller;


import com.trustline.trustline.appuser.dto.*;
import com.trustline.trustline.appuser.service.EmailService;
import com.trustline.trustline.appuser.service.UserService;
import jakarta.validation.Valid;
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

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @Validated @RequestBody OtpRequest otpRequest
    ) {
        return userService.verifyOtp(otpRequest);
    }

    @PostMapping("/verify-password-otp")
    public String verifyPasswordOtp(@Validated @RequestBody OtpRequest otpRequest){
        return userService.resetPasswordOtp(otpRequest);
    }

    @PostMapping("/login")
    public LoginRes<UserResponseDto> login(@Validated @RequestBody LoginReq loginReq) {
        return userService.login(loginReq);
    }

    //    TODO: forgot password to include
    @PostMapping("/forgot-password")
    public UserResponseDto forgotPassword(@Valid @RequestBody ForgotPasswordReq forgotPasswordReq) {
        return UserResponseDto.fromUser(userService.forgotPassword(forgotPasswordReq));
    }

    @PostMapping("/reset-password")
    public UserResponseDto resetPassword(@Valid @RequestBody ResetPasswordReq resetPasswordReq) {
        return UserResponseDto.fromUser(userService.resetPassword(resetPasswordReq));
    }



//  TODO:  forgot password request to generate OTP
//    TODO: after successfully validating OTP, reset password with new password is provided


}
