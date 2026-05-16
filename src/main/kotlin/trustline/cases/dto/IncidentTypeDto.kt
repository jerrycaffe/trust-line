package trustline.cases.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import trustline.appuser.model.UserModel
import trustline.cases.model.IncidentTypesModel
import trustline.institution.model.InstitutionModel
import java.util.UUID

data class CreateIncidentTypeReq(
    @field:NotBlank(message = "name is required")
    val name: String? = null,
    @field:NotBlank(message = "description is required")
    val description: String? = null,
    val steps: Int? = 1,
    @field:NotEmpty(message = "At least one unit is required")
    val unitIds: List<UUID> = emptyList()
)

fun CreateIncidentTypeReq.toIncidentTypeModel(institution: InstitutionModel, createdBy: UserModel) =
    IncidentTypesModel(
        name = name!!,
        description = description!!,
        institution = institution,
        createdBy = createdBy,
        steps = steps!!
    )


data class UpdateIncidentTypeReq(
    val name: String? = null,
    val description: String? = null,
    val steps: Int? = null,
    @field:NotEmpty(message = "At least one unit is required")
    val unitIds: List<UUID> = emptyList()
)

data class UpdateIncidentTypeUnitsReq(
    val unitIds: List<UUID> = emptyList()
)
