package trustline.appuser;


import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trustline.appuser.dto.*
import trustline.appuser.service.UserService


@RestController
@RequestMapping("api/v1/auth")
class UserController(
    private val userService: UserService
) {


    @PostMapping("/register")
    fun register(@RequestBody @Validated registerUserDto: RegisterUserDto): UserResponseDto {
        val createUserRes = userService.createUser(registerUserDto)
        return UserResponseDto.fromUser(createUserRes.user!!, createUserRes.otpId!!);
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(
        @Validated @RequestBody otpRequest: OtpRequest
    ): OtpVerificationResponse {
        return userService.verifyOtp(otpRequest);
    }

    @PostMapping("/resend-otp")
    fun verifyOtp(
        @Validated @RequestBody resendOtpRequest: ResendOtpRequest
    ): OtpVerificationResponse {
        return userService.resendOtp(resendOtpRequest);
    }


    @PostMapping("/login")
    fun login(@Validated @RequestBody loginReq: LoginReq): LoginRes<UserResponseDto> {
        return userService.login(loginReq);
    }

    //    TODO: forgot password to include
    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody forgotPasswordReq: ForgotPasswordReq): ForgotPasswordRes {
        return userService.forgotPassword(forgotPasswordReq);
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody resetPasswordReq: ResetPasswordReq): UserResponseDto {
        return UserResponseDto.fromUser(userService.resetPassword(resetPasswordReq));
    }


//  TODO:  forgot password request to generate OTP
//    Forgot password
//    TODO: after successfully validating OTP, reset password with new password is provided


}
