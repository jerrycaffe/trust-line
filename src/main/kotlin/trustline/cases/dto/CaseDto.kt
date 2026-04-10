package trustline.cases.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.*

data class CreateCaseDto(
    @field:NotNull(message = "Incident type ID is required")
    val incidentTypeId: UUID,

    @field:NotNull(message = "Date of incident is required")
    val dateOfIncident: LocalDate,

    @field:NotBlank(message = "Location is required")
    val location: String,

    @field:NotBlank(message = "Description is required")
    val description: String,

    val uploadUrl: String? = null
)

data class UpdateCaseDto(
    val incidentTypeId: UUID? = null,
    val dateOfIncident: LocalDate? = null,
    val location: String? = null,
    val description: String? = null,
    val uploadUrl: String? = null
)

//data class CaseResponseDto(
//    val id: UUID?,
//    val incidentType: IncidentTypeResponseDto,
//    val dateOfIncident: LocalDate,
//    val location: String,
//    val description: String,
//    val uploadUrl: String?,
//    val reportedBy: UUID?,
//    val createdAt: LocalDateTime?,
//    val updatedAt: LocalDateTime?
//) {
//    companion object {
//        fun fromEntity(case: Case): CaseResponseDto =
//            CaseResponseDto(
//                id = case.id,
//                incidentType = IncidentTypeResponseDto.fromEntity(case.incidentType),
//                dateOfIncident = case.dateOfIncident,
//                location = case.location,
//                description = case.description,
//                uploadUrl = case.uploadUrl,
//                reportedBy = case.reportedBy,
//                createdAt = case.createdAt,
//                updatedAt = case.updatedAt
//            )
//    }
//}
