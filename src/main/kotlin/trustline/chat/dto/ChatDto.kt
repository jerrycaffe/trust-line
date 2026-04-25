package trustline.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import trustline.chat.model.MessageStatus
import java.time.LocalDateTime
import java.util.*

data class SendMessageRequest(
    // Optional: target a specific existing room (counsellor reply or user follow-up)
    val chatRoomId: UUID? = null,

    // Optional: open a direct room with a known recipient (legacy/admin-to-admin)
    val recipientId: UUID? = null,

    @field:NotBlank(message = "Message content is required")
    val content: String? = null
)

data class ChatMessageDto(
    val id: UUID,
    val senderId: UUID,
    val senderEmail: String,
    val content: String,
    // @JsonProperty ensures Jackson serialises as "isRead" not "read"
    @get:JsonProperty("isRead")
    val isRead: Boolean,
    val messageStatus: MessageStatus,
    val createdAt: LocalDateTime?
)

data class ChatRoomDto(
    val id: UUID,
    // null when the room is OPEN and no counsellor has been assigned yet
    val participant: ChatParticipantDto?,
    val lastMessage: ChatMessageDto?,
    val unreadCount: Long,
    val status: String
)

data class ChatParticipantDto(
    val userId: UUID,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val profileImageUrl: String?
)

// ── Real-time event DTOs ──────────────────────────────────────────────────────

/** Sent to /app/chat.typing; forwarded to the other participant */
data class TypingEvent(
    val chatRoomId: UUID,
    val typing: Boolean          // true = started typing, false = stopped
)

/** Sent to /app/chat.delivered once the client receives a message */
data class DeliveryAckRequest(
    val messageId: UUID
)

/** Pushed to /user/{id}/queue/status when a message status changes */
data class MessageStatusUpdate(
    val messageId: UUID,
    val chatRoomId: UUID,
    val status: MessageStatus
)

/** Pushed to /user/{id}/queue/presence when a contact comes online or offline */
data class PresenceEvent(
    val userId: UUID,
    val online: Boolean
)

/** Pushed to /user/{id}/queue/typing when the other side is typing */
data class TypingNotification(
    val chatRoomId: UUID,
    val senderId: UUID,
    val typing: Boolean
)
