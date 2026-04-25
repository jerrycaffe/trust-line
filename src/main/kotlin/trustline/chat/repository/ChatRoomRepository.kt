package trustline.chat.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import trustline.chat.model.ChatRoomModel
import trustline.chat.model.ChatRoomStatus
import java.util.*

/**
 * Spring Data JPA repository for [ChatRoomModel] entities.
 *
 * Provides JPQL queries for the open-pool chat design where counsellors
 * browse and claim unassigned rooms without knowing the specific user.
 */
@Repository
interface ChatRoomRepository : JpaRepository<ChatRoomModel, UUID> {

    /**
     * Finds the direct room between [userOne] and [userTwo] regardless of which
     * side initiated the conversation.
     */
    @Query("""
        SELECT c FROM ChatRoomModel c 
        WHERE (c.participantOne.id = :userOne AND c.participantTwo.id = :userTwo)
           OR (c.participantOne.id = :userTwo AND c.participantTwo.id = :userOne)
    """)
    fun findByParticipants(userOne: UUID, userTwo: UUID): ChatRoomModel?

    /** Returns the user's existing open (unassigned) room, if any, for open-pool deduplication. */
    fun findByParticipantOneIdAndStatus(participantOneId: UUID, status: ChatRoomStatus): ChatRoomModel?

    /** Returns all rooms where [userId] participates (either side), ordered newest first. */
    @Query("""
        SELECT c FROM ChatRoomModel c 
        WHERE c.participantOne.id = :userId OR c.participantTwo.id = :userId
        ORDER BY c.updatedAt DESC
    """)
    fun findByUserId(userId: UUID): List<ChatRoomModel>

    /** Returns all OPEN rooms waiting for a counsellor, oldest first (FIFO queue). */
    @Query("""
        SELECT c FROM ChatRoomModel c 
        WHERE c.status = 'OPEN'
        ORDER BY c.createdAt ASC
    """)
    fun findOpenRooms(): List<ChatRoomModel>
}
