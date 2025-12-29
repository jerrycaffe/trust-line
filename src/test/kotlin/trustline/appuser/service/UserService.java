package trustline.appuser.service;

import trustline.appuser.dto.*;
import trustline.appuser.model.User;

public interface UserService {
    CreateUserRes createUser(RegisterUserDto user);

    LoginRes<UserResponseDto> login(LoginReq loginReq);

    OtpVerificationResponse verifyOtp(OtpRequest otpRequest);

    ForgotPasswordRes forgotPassword(ForgotPasswordReq forgotPasswordReq);

    User resetPassword(ResetPasswordReq resetPasswordReq);

    OtpVerificationResponse resendOtp(ResendOtpRequest resendOtpRequest);
}
