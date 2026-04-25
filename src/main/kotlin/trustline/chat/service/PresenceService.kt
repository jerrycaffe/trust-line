package trustline.chat.service

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import trustline.chat.dto.PresenceEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks WebSocket session presence for online / offline status.
 *
 * Maintains an in-memory map of `userId → Set<sessionId>` so that a user with
 * multiple open tabs/devices is only considered **offline** when their **last**
 * session closes.  [WebSocketEventListener] feeds events into this service on
 * every STOMP CONNECT and DISCONNECT.
 *
 * Contacts are notified of presence changes via
 * `/user/{contactId}/queue/presence` as a [PresenceEvent] payload.
 */
@Service
class PresenceService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    // userId → set of active session IDs (a user can have multiple tabs/devices)
    private val sessions = ConcurrentHashMap<UUID, MutableSet<String>>()

    /** Registers a new WebSocket session for [userId].  Safe for concurrent use. */
    fun registerSession(userId: UUID, sessionId: String) {
        sessions.getOrPut(userId) { ConcurrentHashMap.newKeySet() }.add(sessionId)
    }

    /**
     * Removes a session for [userId].
     * @return `true` if this was the user's **last** active session (user went offline),
     *         `false` if the user still has other open sessions or was already unknown.
     */
    fun removeSession(userId: UUID, sessionId: String): Boolean {
        val userSessions = sessions[userId] ?: return false
        userSessions.remove(sessionId)
        if (userSessions.isEmpty()) {
            sessions.remove(userId)
            return true   // last session gone → user went offline
        }
        return false      // still has other open sessions
    }

    /** Returns `true` if [userId] has at least one active WebSocket session. */
    fun isOnline(userId: UUID): Boolean = sessions.containsKey(userId)

    /**
     * Pushes a [PresenceEvent] to each [contactIds] user on
     * `/user/{contactId}/queue/presence`.
     */
    fun notifyContacts(userId: UUID, online: Boolean, contactIds: List<UUID>) {
        val event = PresenceEvent(userId = userId, online = online)
        contactIds.forEach { contactId ->
            messagingTemplate.convertAndSendToUser(contactId.toString(), "/queue/presence", event)
        }
    }

    /** Returns IDs of all users currently tracked as online */
    fun onlineUsers(): Set<UUID> = sessions.keys.toSet()
}
