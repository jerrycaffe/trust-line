package trustline.service

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.messaging.simp.SimpMessagingTemplate
import trustline.appuser.dto.AuthProvider
import trustline.appuser.model.RoleModel
import trustline.appuser.model.UserModel
import trustline.appuser.service.UserService
import trustline.chat.dto.SendMessageRequest
import trustline.chat.model.*
import trustline.chat.repository.ChatMessageRepository
import trustline.chat.repository.ChatRoomRepository
import trustline.chat.service.ChatServiceImpl
import trustline.chat.service.PresenceService
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.security.AuthDetailsResponse
import trustline.config.security.JWTConfigService
import trustline.institution.model.InstitutionModel
import java.util.*

/**
 * Unit tests for [ChatServiceImpl].
 *
 * All dependencies are MockK mocks.  [SimpMessagingTemplate] uses
 * `relaxed = true` so WebSocket dispatch calls don't require individual stubs.
 */
class ChatServiceImplTest {

    // ── collaborators ────────────────────────────────────────────────────────
    private val chatRoomRepository: ChatRoomRepository = mockk()
    private val chatMessageRepository: ChatMessageRepository = mockk()
    private val userServiceDelegate: UserService = mockk()
    private val jwtConfigService: JWTConfigService = mockk()
    private val messagingTemplate: SimpMessagingTemplate = mockk(relaxed = true)
    private val presenceService: PresenceService = mockk()

    private lateinit var chatService: ChatServiceImpl

    // ── shared fixtures ──────────────────────────────────────────────────────
    private val senderId: UUID = UUID.randomUUID()
    private val recipientId: UUID = UUID.randomUUID()
    private val roomId: UUID = UUID.randomUUID()
    private val messageId: UUID = UUID.randomUUID()
    private val institutionId: UUID = UUID.randomUUID()
    private val institution = InstitutionModel(id = institutionId, name = "Test Corp")
    private val authDetails = AuthDetailsResponse(senderId, institutionId, "sender@test.com")

    @BeforeEach
    fun setUp() {
        chatService = ChatServiceImpl(
            chatRoomRepository,
            chatMessageRepository,
            userServiceDelegate,
            jwtConfigService,
            messagingTemplate,
            presenceService
        )
        every { jwtConfigService.getAuthDetails() } returns authDetails
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun makeUser(
        id: UUID,
        email: String = "user@test.com",
        roleName: String = "User"
    ) = UserModel(
        id = id,
        email = email,
        authProvider = AuthProvider.LOCAL,
        institution = institution,
        roles = mutableSetOf(RoleModel(id = UUID.randomUUID(), name = roleName))
    )

    private fun makeRoom(
        id: UUID = roomId,
        p1: UserModel,
        p2: UserModel? = null,
        status: ChatRoomStatus = ChatRoomStatus.ACTIVE
    ) = ChatRoomModel(id = id, participantOne = p1, participantTwo = p2, status = status)

    private fun makeMessage(
        id: UUID = messageId,
        room: ChatRoomModel,
        sender: UserModel,
        status: MessageStatus = MessageStatus.SENT
    ) = ChatMessageModel(
        id = id,
        chatRoom = room,
        sender = sender,
        content = "Hello",
        messageStatus = status
    )

    // ── sendMessage ───────────────────────────────────────────────────────────

    @Test
    fun `sendMessage with recipientId when recipient is online sets status DELIVERED`() {
        val sender = makeUser(senderId)
        val recipient = makeUser(recipientId, email = "recipient@test.com")
        val savedRoom = makeRoom(p1 = sender, p2 = recipient)
        val savedMessage = makeMessage(room = savedRoom, sender = sender, status = MessageStatus.DELIVERED)

        every { userServiceDelegate.getUserById(senderId) } returns sender
        every { userServiceDelegate.getUserById(recipientId) } returns recipient
        every { chatRoomRepository.findByParticipants(senderId, recipientId) } returns null
        every { chatRoomRepository.save(any()) } returns savedRoom
        every { presenceService.isOnline(recipientId) } returns true

        val messageSlot = slot<ChatMessageModel>()
        every { chatMessageRepository.save(capture(messageSlot)) } returns savedMessage

        val req = SendMessageRequest(recipientId = recipientId, content = "Hello")
        val result = chatService.sendMessage(req)

        assertEquals(MessageStatus.DELIVERED, messageSlot.captured.messageStatus)
        assertEquals(MessageStatus.DELIVERED, result.messageStatus)
    }

    @Test
    fun `sendMessage with recipientId when recipient is offline sets status SENT`() {
        val sender = makeUser(senderId)
        val recipient = makeUser(recipientId, email = "recipient@test.com")
        val savedRoom = makeRoom(p1 = sender, p2 = recipient)
        val savedMessage = makeMessage(room = savedRoom, sender = sender, status = MessageStatus.SENT)

        every { userServiceDelegate.getUserById(senderId) } returns sender
        every { userServiceDelegate.getUserById(recipientId) } returns recipient
        every { chatRoomRepository.findByParticipants(senderId, recipientId) } returns null
        every { chatRoomRepository.save(any()) } returns savedRoom
        every { presenceService.isOnline(recipientId) } returns false

        val messageSlot = slot<ChatMessageModel>()
        every { chatMessageRepository.save(capture(messageSlot)) } returns savedMessage

        val req = SendMessageRequest(recipientId = recipientId, content = "Hello")
        chatService.sendMessage(req)

        assertEquals(MessageStatus.SENT, messageSlot.captured.messageStatus)
    }

    @Test
    fun `sendMessage with no roomId or recipientId creates open-pool room`() {
        val sender = makeUser(senderId)
        val openRoom = makeRoom(p1 = sender, p2 = null, status = ChatRoomStatus.OPEN)
        val savedMessage = makeMessage(room = openRoom, sender = sender, status = MessageStatus.SENT)

        every { userServiceDelegate.getUserById(senderId) } returns sender
        every { chatRoomRepository.findByParticipantOneIdAndStatus(senderId, ChatRoomStatus.OPEN) } returns null
        every { chatRoomRepository.save(any()) } returns openRoom
        // No recipient → presenceService.isOnline never called
        every { chatMessageRepository.save(any()) } returns savedMessage

        val req = SendMessageRequest(content = "I need help")
        val result = chatService.sendMessage(req)

        assertEquals(MessageStatus.SENT, result.messageStatus)
        //  message is broadcast to /topic/open-rooms, not a specific user
        verify { messagingTemplate.convertAndSend("/topic/open-rooms", any<Any>()) }
    }

    // ── getChatMessages ───────────────────────────────────────────────────────

    @Test
    fun `getChatMessages returns messages for valid participant`() {
        val sender = makeUser(senderId)
        val recipient = makeUser(recipientId, email = "r@test.com")
        val room = makeRoom(p1 = sender, p2 = recipient)
        val message = makeMessage(room = room, sender = sender)

        every { chatRoomRepository.findById(roomId) } returns Optional.of(room)
        every { chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId) } returns listOf(message)

        val result = chatService.getChatMessages(roomId)

        assertEquals(1, result.size)
        assertEquals(senderId, result[0].senderId)
    }

    @Test
    fun `getChatMessages throws NotFoundException when caller is not a participant`() {
        val otherUser = makeUser(UUID.randomUUID(), email = "other@test.com")
        val anotherUser = makeUser(UUID.randomUUID(), email = "another@test.com")
        // Room belongs to two OTHER users; senderId is not among them
        val room = makeRoom(p1 = otherUser, p2 = anotherUser)

        every { chatRoomRepository.findById(roomId) } returns Optional.of(room)

        assertThrows<NotFoundException> { chatService.getChatMessages(roomId) }
    }

    // ── claimRoom ─────────────────────────────────────────────────────────────

    @Test
    fun `claimRoom transitions OPEN room to ACTIVE and assigns counsellor`() {
        val user = makeUser(UUID.randomUUID(), email = "user@test.com")
        val counsellor = makeUser(senderId, email = "admin@test.com", roleName = "Administrator")
        val openRoom = makeRoom(id = roomId, p1 = user, p2 = null, status = ChatRoomStatus.OPEN)
        // Create the expected saved room separately to avoid modifying openRoom at setup time
        val claimedRoom = makeRoom(id = roomId, p1 = user, p2 = counsellor, status = ChatRoomStatus.ACTIVE)

        every { userServiceDelegate.getUserById(senderId) } returns counsellor
        every { chatRoomRepository.findById(roomId) } returns Optional.of(openRoom)
        every { chatRoomRepository.save(any()) } returns claimedRoom
        every { chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId) } returns emptyList()
        every { chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(roomId, senderId) } returns 0L

        val result = chatService.claimRoom(roomId)

        assertEquals("ACTIVE", result.status)
        verify { chatRoomRepository.save(withArg { it.status == ChatRoomStatus.ACTIVE }) }
    }

    @Test
    fun `claimRoom throws BadRequestException when caller is not Administrator`() {
        val regularUser = makeUser(senderId, roleName = "User")

        every { userServiceDelegate.getUserById(senderId) } returns regularUser

        assertThrows<BadRequestException> { chatService.claimRoom(roomId) }
        verify(exactly = 0) { chatRoomRepository.findById(any()) }
    }

    @Test
    fun `claimRoom throws BadRequestException when room is not OPEN`() {
        val counsellor = makeUser(senderId, roleName = "Administrator")
        val activeRoom = makeRoom(p1 = makeUser(UUID.randomUUID()), status = ChatRoomStatus.ACTIVE)

        every { userServiceDelegate.getUserById(senderId) } returns counsellor
        every { chatRoomRepository.findById(roomId) } returns Optional.of(activeRoom)

        assertThrows<BadRequestException> { chatService.claimRoom(roomId) }
        verify(exactly = 0) { chatRoomRepository.save(any()) }
    }

    // ── markDelivered ─────────────────────────────────────────────────────────

    @Test
    fun `markDelivered transitions SENT message to DELIVERED and notifies sender`() {
        val senderUser = makeUser(senderId)
        val room = makeRoom(p1 = senderUser, status = ChatRoomStatus.ACTIVE)
        val message = makeMessage(room = room, sender = senderUser, status = MessageStatus.SENT)

        every { chatMessageRepository.findMessageById(messageId) } returns message
        every { chatMessageRepository.updateMessageStatus(messageId, MessageStatus.DELIVERED) } just Runs

        chatService.markDelivered(messageId)

        verify { chatMessageRepository.updateMessageStatus(messageId, MessageStatus.DELIVERED) }
        verify { messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/status", any<Any>()) }
    }

    @Test
    fun `markDelivered is a no-op when message already DELIVERED`() {
        val senderUser = makeUser(senderId)
        val room = makeRoom(p1 = senderUser)
        val message = makeMessage(room = room, sender = senderUser, status = MessageStatus.DELIVERED)

        every { chatMessageRepository.findMessageById(messageId) } returns message

        chatService.markDelivered(messageId)

        verify(exactly = 0) { chatMessageRepository.updateMessageStatus(any(), any()) }
    }

    @Test
    fun `markDelivered is a no-op when message not found`() {
        every { chatMessageRepository.findMessageById(messageId) } returns null

        chatService.markDelivered(messageId)

        verify(exactly = 0) { chatMessageRepository.updateMessageStatus(any(), any()) }
    }
}
