package trustline.institution.dto

import jakarta.validation.constraints.NotBlank
import trustline.institution.model.UnitModel
import java.util.UUID

data class CreateUnitReq(
    @field:NotBlank(message = "name is required")
    val name: String? = null
)

data class UpdateUnitReq(
    @field:NotBlank(message = "name is required")
    val name: String? = null
)

data class UnitResponseDto(
    val id: UUID?,
    val name: String?,
    val institutionId: UUID?
)

fun UnitModel.toUnitResponse() = UnitResponseDto(
    id = id,
    name = name,
    institutionId = institution.id
)
