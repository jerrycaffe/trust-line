package trustline.institution.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import trustline.institution.model.InstitutionModel

data class CreateInstitutionDto(
    @field:NotNull(message = "name is required")
    @field:NotBlank(message = "name is required")
    val name: String? = null,
    @field:NotNull(message = "contact phone is required")
    @field:NotBlank(message = "contact phone is required")
    val contactNumber: String? = null,
    @field:NotNull(message = "address is required")
    @field:NotBlank(message = "address is required")
    val address: String? = null
)

fun CreateInstitutionDto.toInstitutionModel(): InstitutionModel = InstitutionModel(
    name = name?.uppercase(), address = address, phoneNumber = contactNumber
)