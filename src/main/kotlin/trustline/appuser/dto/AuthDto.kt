package trustline.appuser.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class LoginRes<T>(
    val accessToken: String? = null,
    val message: String? = null,
    val data: T? = null
)

data class LoginReq(
    val userName: String? = null,
    val password: String? = null
)

data class OtpRequest(
    @field:NotNull(message = "verification id is required")
    val verificationId: String? = null,
    @field:NotNull(message = "user id is required")
    val userId: UUID? = null
)

data class OtpVerificationResponse(
    val message: String? = null,
    val otpId: UUID? = null
)

enum class OtpModeEnum {
    SMS, EMAIL
}

enum class VerificationType {
    REGISTER, RESET_PASSWORD
}

enum class Status {
    OTP_VALIDATION, DISABLED, VERIFIED
}

enum class AuthProvider {
    LOCAL, GOOGLE
}

data class ForgotPasswordReq(
    @field:NotNull(message = "email is required")
    val email: String? = null
)

data class ForgotPasswordRes(
    val otpId: UUID? = null
)

data class ResendOtpRequest(
    @field:NotNull(message = "Previous OTP ID is required")
    val prevOtpId: UUID? = null
)


data class ResetPasswordReq(
    @field:NotBlank(message = "newPassword field is expected")
    val newPassword: String? = null,
    @field:NotBlank(message = "userName field is required")
    val userName: String? = null
)