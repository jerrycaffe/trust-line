package trustline.cases.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class CreateCaseDto(
    @field:NotNull(message = "Incident type ID is required")
    val incidentTypeId: UUID? = null,

    @field:NotNull(message = "Date of incident is required")
    val dateOfIncident: LocalDate? = null,

    @field:NotBlank(message = "Location is required")
    val location: String? = null,

    @field:NotBlank(message = "Description is required")
    val description: String? = null
)

data class CaseResponseDto(
    val id: UUID,
    val incidentType: String,
    val dateOfIncident: LocalDateTime?,
    val location: String,
    val description: String,
    val reportedBy: String,
    val status: String?,
    val closed: Boolean,
    val currentUnit: String?,
    val nextUnit: String?,
    val files: List<FileDto>,
    val comments: List<CommentResponseDto>,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

data class FileDto(
    val id: UUID,
    val url: String
)

data class CreateCommentRequest(
    @field:NotBlank(message = "Comment is required")
    val comment: String? = null,
    val nextUnitId: UUID? = null
)

data class CommentResponseDto(
    val id: UUID,
    val comment: String,
    val commenterEmail: String,
    val createdAt: LocalDateTime?
)
