package trustline.service

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.messaging.simp.SimpMessagingTemplate
import trustline.chat.dto.PresenceEvent
import trustline.chat.service.PresenceService
import java.util.*

/**
 * Unit tests for [PresenceService].
 *
 * Uses the real [PresenceService] implementation with a relaxed MockK for
 * [SimpMessagingTemplate] so that WebSocket dispatch can be verified without
 * actually sending messages.
 */
class PresenceServiceTest {

    private val messagingTemplate: SimpMessagingTemplate = mockk(relaxed = true)
    private lateinit var presenceService: PresenceService

    @BeforeEach
    fun setUp() {
        // Re-create for each test to get clean in-memory state
        presenceService = PresenceService(messagingTemplate)
    }

    // ── registerSession / isOnline ────────────────────────────────────────────

    @Test
    fun `registerSession makes user online`() {
        val userId = UUID.randomUUID()
        presenceService.registerSession(userId, "session-1")
        assertTrue(presenceService.isOnline(userId))
    }

    @Test
    fun `user is offline before any session is registered`() {
        assertFalse(presenceService.isOnline(UUID.randomUUID()))
    }

    @Test
    fun `registering multiple sessions keeps user online`() {
        val userId = UUID.randomUUID()
        presenceService.registerSession(userId, "session-1")
        presenceService.registerSession(userId, "session-2")
        assertTrue(presenceService.isOnline(userId))
    }

    // ── removeSession ─────────────────────────────────────────────────────────

    @Test
    fun `removeSession returns true and marks user offline when last session closes`() {
        val userId = UUID.randomUUID()
        presenceService.registerSession(userId, "session-1")

        val wentOffline = presenceService.removeSession(userId, "session-1")

        assertTrue(wentOffline)
        assertFalse(presenceService.isOnline(userId))
    }

    @Test
    fun `removeSession returns false when user still has other sessions`() {
        val userId = UUID.randomUUID()
        presenceService.registerSession(userId, "session-1")
        presenceService.registerSession(userId, "session-2")

        val wentOffline = presenceService.removeSession(userId, "session-1")

        assertFalse(wentOffline)
        assertTrue(presenceService.isOnline(userId))  // still has session-2
    }

    @Test
    fun `removeSession returns false for unknown user`() {
        val notRegistered = UUID.randomUUID()
        val wentOffline = presenceService.removeSession(notRegistered, "session-x")
        assertFalse(wentOffline)
    }

    // ── notifyContacts ────────────────────────────────────────────────────────

    @Test
    fun `notifyContacts pushes online event to every contact`() {
        val userId = UUID.randomUUID()
        val contact1 = UUID.randomUUID()
        val contact2 = UUID.randomUUID()

        presenceService.notifyContacts(userId, online = true, contactIds = listOf(contact1, contact2))

        verify { messagingTemplate.convertAndSendToUser(contact1.toString(), "/queue/presence", PresenceEvent(userId, true)) }
        verify { messagingTemplate.convertAndSendToUser(contact2.toString(), "/queue/presence", PresenceEvent(userId, true)) }
    }

    @Test
    fun `notifyContacts pushes offline event`() {
        val userId = UUID.randomUUID()
        val contact = UUID.randomUUID()

        presenceService.notifyContacts(userId, online = false, contactIds = listOf(contact))

        verify { messagingTemplate.convertAndSendToUser(contact.toString(), "/queue/presence", PresenceEvent(userId, false)) }
    }

    @Test
    fun `notifyContacts with empty list sends nothing`() {
        val userId = UUID.randomUUID()
        presenceService.notifyContacts(userId, online = true, contactIds = emptyList())
        verify(exactly = 0) { messagingTemplate.convertAndSendToUser(any(), any(), any<Any>()) }
    }

    // ── onlineUsers ───────────────────────────────────────────────────────────

    @Test
    fun `onlineUsers reflects currently registered users`() {
        val user1 = UUID.randomUUID()
        val user2 = UUID.randomUUID()
        presenceService.registerSession(user1, "s1")
        presenceService.registerSession(user2, "s2")

        val online = presenceService.onlineUsers()
        assertTrue(online.containsAll(listOf(user1, user2)))
    }

    @Test
    fun `onlineUsers excludes users whose last session closed`() {
        val user = UUID.randomUUID()
        presenceService.registerSession(user, "s1")
        presenceService.removeSession(user, "s1")

        assertFalse(presenceService.onlineUsers().contains(user))
    }
}
