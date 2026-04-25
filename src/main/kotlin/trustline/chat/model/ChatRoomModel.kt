package trustline.chat.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import trustline.appuser.model.UserModel
import java.util.*

/**
 * Life-cycle states of a [ChatRoomModel].
 *
 * - [OPEN]   – Created by a user; no counsellor assigned yet.
 *              New messages are broadcast to `/topic/open-rooms` so any
 *              available counsellor can pick them up.
 * - [ACTIVE] – A counsellor has claimed the room ([participantTwo] is set).
 *              Messages flow directly between the two participants.
 * - [CLOSED] – Conversation ended; no further messages expected.
 */
enum class ChatRoomStatus {
    OPEN, ACTIVE, CLOSED
}

/**
 * JPA entity representing a chat room between a user and a counsellor.
 *
 * The **open-pool** design means [participantTwo] (the counsellor) starts as
 * `null` and [status] starts as [ChatRoomStatus.OPEN].  Any available
 * counsellor can then call `POST /api/v1/chat/rooms/{id}/claim`, which sets
 * [participantTwo] and transitions [status] to [ChatRoomStatus.ACTIVE].
 *
 * A user may have at most **one** [ChatRoomStatus.OPEN] room at a time;
 * subsequent messages without an explicit `chatRoomId` reuse that room.
 */
@Entity
@Table(name = "chat_rooms")
data class ChatRoomModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_one")
    val participantOne: UserModel,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_two")
    var participantTwo: UserModel? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ChatRoomStatus = ChatRoomStatus.OPEN
) : AuditModel()
