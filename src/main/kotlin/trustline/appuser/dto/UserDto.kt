package trustline.appuser.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import trustline.appuser.model.UserModel
import trustline.institution.model.InstitutionModel
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
    @field:NotNull(message = "institution id is required")
    val institutionId: UUID
)

fun RegisterUserDto.toUserModel(encryptedPassword: String, institution: InstitutionModel) = UserModel(
    email = email!!,
    password = encryptedPassword,
    phoneNumber = phoneNumber,
    institution = institution,
    authProvider = AuthProvider.LOCAL,
    status = Status.OTP_VALIDATION,
)


enum class Gender {
    MALE, FEMALE
}

data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val gender: Gender? = null
)
