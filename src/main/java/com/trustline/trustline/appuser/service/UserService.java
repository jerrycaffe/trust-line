package com.trustline.trustline.appuser.service;

import com.trustline.trustline.appuser.dto.*;
import com.trustline.trustline.appuser.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {
    User createUser(RegisterUserDto user);

    LoginRes<UserResponseDto> login(LoginReq loginReq);

    String verifyOtp(OtpRequest otpRequest);
}
