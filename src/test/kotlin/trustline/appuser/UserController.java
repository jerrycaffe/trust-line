package trustline.appuser;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trustline.appuser.dto.*;
import trustline.appuser.service.UserService;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/auth")
public class UserController {

    private final UserService userService;


    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Validated RegisterUserDto registerUserDto) {
        CreateUserRes createUserRes = userService.createUser(registerUserDto);
        return UserResponseDto.fromUser(createUserRes.getUser(), createUserRes.getOtpId());
    }

    @PostMapping("/verify-otp")
    public OtpVerificationResponse verifyOtp(
            @Validated @RequestBody OtpRequest otpRequest
    ) {
        return userService.verifyOtp(otpRequest);
    }

    @PostMapping("/resend-otp")
    public OtpVerificationResponse verifyOtp(
            @Validated @RequestBody ResendOtpRequest resendOtpRequest
    ) {
        return userService.resendOtp(resendOtpRequest);
    }



    @PostMapping("/login")
    public LoginRes<UserResponseDto> login(@Validated @RequestBody LoginReq loginReq) {
        return userService.login(loginReq);
    }

    //    TODO: forgot password to include
    @PostMapping("/forgot-password")
    public ForgotPasswordRes forgotPassword(@Valid @RequestBody ForgotPasswordReq forgotPasswordReq) {

        return userService.forgotPassword(forgotPasswordReq);
    }

    @PostMapping("/reset-password")
    public UserResponseDto resetPassword(@Valid @RequestBody ResetPasswordReq resetPasswordReq) {
        return UserResponseDto.fromUser(userService.resetPassword(resetPasswordReq));
    }



//  TODO:  forgot password request to generate OTP
//    Forgot password
//    TODO: after successfully validating OTP, reset password with new password is provided


}
