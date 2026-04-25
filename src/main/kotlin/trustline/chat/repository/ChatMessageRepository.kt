package trustline.chat.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import trustline.chat.model.ChatMessageModel
import trustline.chat.model.MessageStatus
import java.util.*

/**
 * Spring Data JPA repository for [ChatMessageModel] entities.
 *
 * Includes modifying queries for bulk read-marking and individual status
 * transitions that drive the SENT → DELIVERED → READ life-cycle.
 */
@Repository
interface ChatMessageRepository : JpaRepository<ChatMessageModel, UUID> {
    /** Returns all messages in a room ordered chronologically (ascending). */
    fun findByChatRoomIdOrderByCreatedAtAsc(chatRoomId: UUID): List<ChatMessageModel>

    /**
     * Bulk-marks as READ all unread messages in [chatRoomId] that were NOT sent
     * by [userId] (the caller is the recipient marking their own unread messages).
     * Also syncs [ChatMessageModel.isRead] to `true` for backwards compatibility.
     */
    @Modifying
    @Query("UPDATE ChatMessageModel m SET m.isRead = true, m.messageStatus = 'READ' WHERE m.chatRoom.id = :chatRoomId AND m.sender.id != :userId AND m.isRead = false")
    fun markMessagesAsRead(chatRoomId: UUID, userId: UUID)

    /**
     * Updates [messageId]'s status to [status], but never downgrades a READ message.
     * Guard clause (`<> 'READ'`) prevents DELIVERED from overwriting READ.
     */
    @Modifying
    @Query("UPDATE ChatMessageModel m SET m.messageStatus = :status WHERE m.id = :messageId AND m.messageStatus <> 'READ'")
    fun updateMessageStatus(messageId: UUID, status: MessageStatus)

    /** Single-message lookup by ID — returns null if the message does not exist. */
    @Query("SELECT m FROM ChatMessageModel m WHERE m.id = :messageId")
    fun findMessageById(messageId: UUID): ChatMessageModel?

    /** Counts unread messages in [chatRoomId] that the caller did not send (their inbox count). */
    fun countByChatRoomIdAndSenderIdNotAndIsReadFalse(chatRoomId: UUID, senderId: UUID): Long
}
