package trustline.cases.dto

import jakarta.validation.constraints.NotBlank
import trustline.appuser.model.UserModel
import trustline.cases.model.IncidentTypesModel
import trustline.institution.model.InstitutionModel

data class CreateIncidentTypeReq(
    @field:NotBlank(message = "name is required")
    val name: String? = null,
    @field:NotBlank(message = "description is required")
    val description: String? = null,
    val steps: Int? = 1
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
    val steps: Int? = null
)
