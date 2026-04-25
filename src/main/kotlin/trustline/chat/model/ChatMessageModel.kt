package trustline.chat.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import trustline.appuser.model.UserModel
import java.util.*

/**
 * Life-cycle states of a [ChatMessageModel].
 *
 * Status transitions always move **forward**: SENT → DELIVERED → READ.
 *
 * - [SENT]      – Message persisted; the recipient was offline at send time.
 * - [DELIVERED] – Recipient's device received the message.  Set immediately if
 *                 the recipient was online when the message was sent, or
 *                 explicitly when the client sends `/app/chat.delivered`.
 * - [READ]      – Recipient called `PUT /api/v1/chat/rooms/{id}/read`,
 *                 bulk-marking messages as read.
 *
 * The original sender is notified of DELIVERED / READ transitions via a
 * [MessageStatusUpdate] pushed to `/user/{senderId}/queue/status`.
 */
enum class MessageStatus { SENT, DELIVERED, READ }

/**
 * JPA entity representing a single chat message within a [ChatRoomModel].
 *
 * [messageStatus] tracks the full delivery/read-receipt life-cycle.
 * The legacy [isRead] boolean is kept in sync: it is set to `true` when
 * [messageStatus] reaches [MessageStatus.READ] via a bulk `UPDATE` query.
 */
@Entity
@Table(name = "chat_messages")
data class ChatMessageModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    val chatRoom: ChatRoomModel,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    val sender: UserModel,
    @Column(name = "content", columnDefinition = "TEXT")
    val content: String,
    @Column(name = "is_read")
    var isRead: Boolean = false,
    @Enumerated(EnumType.STRING)
    @Column(name = "message_status")
    var messageStatus: MessageStatus = MessageStatus.SENT
) : AuditModel()
