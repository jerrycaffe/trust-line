package trustline.appuser.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import trustline.appuser.model.UserResponseDto
import java.util.*

data class LoginRes(
    val accessToken: String? = null,
    val userDetails: UserResponseDto? = null,
    val otpId: UUID? = null,
    val userId: UUID? = null
)

data class TrustlineResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean? = true,
    val code: String? = "00"
)

data class LoginReq(
    @field:NotBlank(message = "username is required")
    @field:NotNull(message = "username is required")
    val userName: String? = null,
    @field:NotBlank(message = "username is required")
    @field:NotNull(message = "username is required")
    val password: String? = null,
    @field:NotNull(message = "insitution id is required")
    val institutionId: UUID? = null
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
    OTP_VALIDATION, DISABLED, VERIFIED, COMPLETED, DISCONTINUED, UNVERIFIED
}

enum class AuthProvider {
    LOCAL, GOOGLE
}

data class ForgotPasswordReq(
    @field:NotNull(message = "email is required")
    @field:NotBlank(message = "email is required")
    val email: String? = null,
    @field:NotNull(message = "institution id is required")
    val institutionId: UUID? = null
)

data class ForgotPasswordRes(
    val otpId: UUID? = null,
    val userId: UUID? = null
)

data class ResendOtpRequest(
    @field:NotNull(message = "Previous OTP ID is required")
    val prevOtpId: UUID? = null
)


data class ResetPasswordReq(
    @field:NotBlank(message = "newPassword field is expected")
    val newPassword: String? = null,
    @field:NotBlank(message = "userName field is required")
    val userName: String? = null,
    @field:NotNull(message = "institution id is required")
    val institutionId: UUID? = null,
    @field:NotNull(message = "token id required")
    val tokenId: UUID? = null
)

data class InviteUserReq(
    @field:NotBlank(message = "Email is required")
    val email: String? = null,
    @field:NotBlank(message = "Role is required")
    val role: String? = null
)

data class InviteUserRes(
    val userId: UUID,
    val email: String,
    val role: String
)

data class ChangeUserRoleReq(
    @field:NotNull(message = "User ID is required")
    val userId: UUID? = null,
    @field:NotBlank(message = "Role is required")
    val role: String? = null
)

data class ChangeUserRoleRes(
    val userId: UUID,
    val email: String,
    val previousRole: String,
    val newRole: String
)

data class RoleDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val institutionId: UUID?,
    val permissions: List<PermissionDto>
)

data class PermissionDto(
    val id: UUID,
    val name: String,
    val description: String,
    val institutionId: UUID?
)

data class CreateRoleRequest(
    @field:NotBlank(message = "Role name is required")
    val name: String,
    @field:NotBlank(message = "Role description is required")
    val description: String
)

data class CreatePermissionRequest(
    @field:NotBlank(message = "Permission name is required")
    val name: String,
    @field:NotBlank(message = "Permission description is required")
    val description: String
)

data class AddPermissionsToRoleRequest(
    @field:NotNull(message = "permissionIds is required")
    val permissionIds: List<UUID>
)