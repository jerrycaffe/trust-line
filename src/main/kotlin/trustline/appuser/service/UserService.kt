package trustline.appuser.service;

import trustline.appuser.dto.*;
import trustline.appuser.model.User;

interface UserService {
    fun createUser(user: RegisterUserDto): CreateUserRes

    fun login(loginReq: LoginReq): LoginRes<UserResponseDto>

    fun verifyOtp(otpRequest: OtpRequest): OtpVerificationResponse;

    fun forgotPassword(forgotPasswordReq: ForgotPasswordReq): ForgotPasswordRes;

    fun resetPassword(resetPasswordReq: ResetPasswordReq): User;

    fun resendOtp(resendOtpRequest: ResendOtpRequest): OtpVerificationResponse;
}
