package trustline.cases.dto

import jakarta.validation.constraints.NotBlank

data class CreateIncidentTypeDto(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Description is required")
    val description: String
)

data class UpdateIncidentTypeDto(
    val name: String? = null,
    val description: String? = null
)

//data class IncidentTypeResponseDto(
//    val id: UUID?,
//    val name: String,
//    val description: String,
//    val createdBy: UUID?,
//    val createdAt: LocalDateTime?,
//    val updatedAt: LocalDateTime?
//) {
//    companion object {
//        fun fromEntity(incidentType: IncidentType): IncidentTypeResponseDto =
//            IncidentTypeResponseDto(
//                id = incidentType.id,
//                name = incidentType.name,
//                description = incidentType.description,
//                createdBy = incidentType.createdBy,
//                createdAt = incidentType.createdAt,
//                updatedAt = incidentType.updatedAt
//            )
//    }
//}
