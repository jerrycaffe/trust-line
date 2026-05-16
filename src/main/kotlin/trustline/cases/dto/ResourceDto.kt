package trustline.cases.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.*

data class CreateResourceRequest(
    @field:NotBlank(message = "Name is required")
    val name: String? = null,

    @field:NotNull(message = "Incident type ID is required")
    val incidentTypeId: UUID? = null,

    val contents: String? = null
)

data class UpdateResourceRequest(
    val name: String? = null,
    val incidentTypeId: UUID? = null,
    val contents: String? = null
)

data class ResourceResponseDto(
    val id: UUID,
    val name: String,
    val incidentType: String,
    val contents: String?,
    val fileUrl: String?,
    val createdBy: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
