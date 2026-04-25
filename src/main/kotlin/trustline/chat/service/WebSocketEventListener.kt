package trustline.chat.service

import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import trustline.chat.repository.ChatRoomRepository
import java.util.*

/**
 * Spring event listener that bridges STOMP lifecycle events to [PresenceService].
 *
 * On [SessionConnectedEvent] the user's session is registered and all contacts
 * in shared chat rooms are notified `online = true`.
 *
 * On [SessionDisconnectEvent] the session is removed.  Contacts are only
 * notified `online = false` if this was the user's **last** active session
 * (multi-device safe).
 *
 * The principal name set by [WebSocketAuthInterceptor] is the user's UUID string,
 * so every event carries the authenticated user identity without an extra DB query.
 */
@Component
class WebSocketEventListener(
    private val presenceService: PresenceService,
    private val chatRoomRepository: ChatRoomRepository
) {

    @EventListener
    fun handleConnect(event: SessionConnectedEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val principal = accessor.user ?: return
        val userId = runCatching { UUID.fromString(principal.name) }.getOrNull() ?: return
        val sessionId = accessor.sessionId ?: return

        presenceService.registerSession(userId, sessionId)

        val contacts = contactsOf(userId)
        presenceService.notifyContacts(userId, online = true, contactIds = contacts)
    }

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val principal = accessor.user ?: return
        val userId = runCatching { UUID.fromString(principal.name) }.getOrNull() ?: return
        val sessionId = accessor.sessionId ?: return

        val wentOffline = presenceService.removeSession(userId, sessionId)
        if (wentOffline) {
            val contacts = contactsOf(userId)
            presenceService.notifyContacts(userId, online = false, contactIds = contacts)
        }
    }

    private fun contactsOf(userId: UUID): List<UUID> {
        return chatRoomRepository.findByUserId(userId).mapNotNull { room ->
            if (room.participantOne.id == userId) room.participantTwo?.id
            else room.participantOne.id
        }.distinct()
    }
}
