package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.*;
import com.trustline.trustline.appuser.model.User;

public interface UserService {
    User createUser(RegisterUserDto user);

    LoginRes<UserResponseDto> login(LoginReq loginReq);

    String verifyOtp(OtpRequest otpRequest);

    User forgotPassword(ForgotPasswordReq forgotPasswordReq);

    User resetPassword(ResetPasswordReq resetPasswordReq);
}
