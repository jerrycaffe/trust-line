package trustline.notification.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.*

data class CreateNotificationRequest(
    @field:NotNull(message = "User ID is required")
    val userId: UUID? = null,

    @field:NotBlank(message = "Topic is required")
    val topic: String? = null,

    @field:NotBlank(message = "Message is required")
    val message: String? = null
)

data class NotificationResponseDto(
    val id: UUID,
    val topic: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime?
)
