package trustline.chat.service

import trustline.chat.dto.*
import java.util.*

/**
 * Business-logic contract for the chat system.
 *
 * Implementations must handle both the REST-initiated paths (HTTP controllers)
 * and the STOMP-initiated paths (WebSocket controllers) since both share the
 * same service layer.
 */
interface ChatService {
    /**
     * Persists and dispatches a new message.
     * Resolves the target room via [SendMessageRequest.chatRoomId],
     * [SendMessageRequest.recipientId], or the open-pool fallback (neither provided).
     * Sets initial [MessageStatus] to DELIVERED if the recipient is online, SENT otherwise.
     */
    fun sendMessage(request: SendMessageRequest): ChatMessageDto

    /** Returns all messages in [chatRoomId] in ascending chronological order. Throws if caller is not a participant. */
    fun getChatMessages(chatRoomId: UUID): List<ChatMessageDto>

    /** Returns all rooms the authenticated user participates in, with unread counts and last message. */
    fun getMyChatRooms(): List<ChatRoomDto>

    /**
     * Marks all messages sent by the other participant in [chatRoomId] as READ,
     * then pushes a [MessageStatusUpdate] to each original sender via WebSocket.
     */
    fun markAsRead(chatRoomId: UUID)

    /** Returns all OPEN (unclaimed) rooms ordered oldest-first, for the counsellor dashboard. */
    fun getOpenRooms(): List<ChatRoomDto>

    /**
     * Assigns the authenticated counsellor to an OPEN room, transitions status to ACTIVE,
     * and notifies the user via `/user/{userId}/queue/messages`.
     * Throws [BadRequestException] if the caller is not an Administrator or the room is not OPEN.
     */
    fun claimRoom(chatRoomId: UUID): ChatRoomDto

    /**
     * Acknowledges delivery of [messageId].
     * Transitions status from SENT → DELIVERED and notifies the original sender.
     * No-ops if the message is already DELIVERED or READ.
     */
    fun markDelivered(messageId: UUID)
}
