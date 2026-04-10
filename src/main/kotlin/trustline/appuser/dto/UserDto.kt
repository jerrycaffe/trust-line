package trustline.appuser.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import trustline.appuser.model.UserModel
import java.util.*

data class CreateUserRes(
     val user: UserModel? = null,
     val otpId: UUID? = null
)

data class RegisterUserDto(
    @field:NotBlank(message = "Email is required")
    @field:Pattern(regexp = "^\\S+@\\S+\\.\\S+$", message = "Email should be like test@test.com")
    val email: String? = null,
    @field:Size(min = 4, message = "password should be more than 3 characters")
    val password: String? = null,
    @field:NotBlank(message = "Phone number field is required")
    @field:Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    val phoneNumber: String? = null,
)


data class UserResponseDto(
    val id: UUID?,
    val email: String?,
    val phoneNumber: String?,
    val status: Status?,
    val otpId: UUID? = null,
    val emailVerified: Boolean
) {
    companion object {

        fun fromUser(user: UserModel, otpId: UUID): UserResponseDto =
            UserResponseDto(
                id = user.id,
                email = user.email,
                phoneNumber = user.phoneNumber,
                status = user.status,
                otpId = otpId,
                emailVerified = user.isAccountVerified
            )

        fun fromUser(user: UserModel): UserResponseDto =
            UserResponseDto(
                id = user.id,
                email = user.email,
                phoneNumber = user.phoneNumber,
                status = user.status,
                emailVerified = user.isAccountVerified
            )
    }
}

enum class Gender {
    MALE, FEMALE
}
