package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.*;
import com.trustline.trustline.appuser.model.User;

public interface UserService {
    CreateUserRes createUser(RegisterUserDto user);

    LoginRes<UserResponseDto> login(LoginReq loginReq);

    OtpVerificationResponse verifyOtp(OtpRequest otpRequest);

    ForgotPasswordRes forgotPassword(ForgotPasswordReq forgotPasswordReq);

    User resetPassword(ResetPasswordReq resetPasswordReq);

    OtpVerificationResponse resendOtp(ResendOtpRequest resendOtpRequest);
}
