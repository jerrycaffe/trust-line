package trustline.appuser.controller;


import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import trustline.appuser.dto.*
import trustline.appuser.model.UserResponseDto
import trustline.appuser.service.UserService


@RestController
@RequestMapping("api/v1/auth")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/register")
    fun register(@RequestBody @Validated registerUserDto: RegisterUserDto): UserResponseDto {
        return userService.createUser(registerUserDto)
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@Validated @RequestBody otpRequest: OtpRequest): OtpVerificationResponse {
        return userService.verifyOtp(otpRequest)
    }

    @PostMapping("/resend-otp")
    fun resendOtp(@Validated @RequestBody resendOtpRequest: ResendOtpRequest): OtpVerificationResponse {
        return userService.resendOtp(resendOtpRequest)
    }

    @PostMapping("/login")
    fun login(@Validated @RequestBody loginReq: LoginReq): LoginRes {
        return userService.login(loginReq)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody forgotPasswordReq: ForgotPasswordReq): ForgotPasswordRes {
        return userService.forgotPassword(forgotPasswordReq)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody resetPasswordReq: ResetPasswordReq): UserResponseDto {
        return userService.resetPassword(resetPasswordReq)
    }
}
