package trustline.chat.service

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import trustline.appuser.model.UserModel
import trustline.appuser.service.UserService
import trustline.chat.dto.*
import trustline.chat.model.ChatMessageModel
import trustline.chat.model.ChatRoomModel
import trustline.chat.model.ChatRoomStatus
import trustline.chat.model.MessageStatus
import trustline.chat.repository.ChatMessageRepository
import trustline.chat.repository.ChatRoomRepository
import trustline.config.exception.BadRequestException
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import java.util.*

/**
 * Core implementation of [ChatService].
 *
 * Shared between the REST controller ([ChatController]) and the WebSocket
 * controller ([ChatWebSocketController]) so that both transports produce
 * identical business outcomes.
 *
 * **Room resolution** (see [resolveRoom]) follows a three-way priority:
 * 1. `chatRoomId` supplied → use that room directly.
 * 2. `recipientId` supplied → find or create a direct (ACTIVE) room.
 * 3. Neither supplied → find or create the sender's OPEN pool room.
 *
 * **Message status** is set at persist time using [PresenceService.isOnline]:
 * DELIVERED if the recipient is connected, SENT otherwise.  Subsequent
 * transitions (DELIVERED, READ) are pushed back to the sender via WebSocket.
 */
@Service
class ChatServiceImpl(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userService: UserService,
    private val jwtConfigService: JWTConfigService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val presenceService: PresenceService
) : ChatService {

    @Transactional
    override fun sendMessage(request: SendMessageRequest): ChatMessageDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val sender = userService.getUserById(authDetails.userId)

        val chatRoom = resolveRoom(request, sender)

        // If recipient is online mark as DELIVERED immediately, otherwise SENT
        val recipient = if (chatRoom.participantOne.id == sender.id) chatRoom.participantTwo else chatRoom.participantOne
        val initialStatus = if (recipient != null && presenceService.isOnline(recipient.id!!))
            MessageStatus.DELIVERED else MessageStatus.SENT

        val message = chatMessageRepository.save(
            ChatMessageModel(chatRoom = chatRoom, sender = sender, content = request.content!!, messageStatus = initialStatus)
        )

        val messageDto = toMessageDto(message)
        dispatchMessage(chatRoom, sender, messageDto)
        return messageDto
    }

    override fun getChatMessages(chatRoomId: UUID): List<ChatMessageDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        val chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow { NotFoundException("Chat room not found") }

        if (chatRoom.participantOne.id != authDetails.userId && chatRoom.participantTwo?.id != authDetails.userId) {
            throw NotFoundException("Chat room not found")
        }

        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId)
            .map { toMessageDto(it) }
    }

    override fun getMyChatRooms(): List<ChatRoomDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        val chatRooms = chatRoomRepository.findByUserId(authDetails.userId)

        return chatRooms.map { room ->
            val otherUser = if (room.participantOne.id == authDetails.userId)
                room.participantTwo else room.participantOne

            val messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(room.id!!)
            val lastMessage = messages.lastOrNull()?.let { toMessageDto(it) }
            val unreadCount = chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(
                room.id!!, authDetails.userId
            )

            ChatRoomDto(
                id = room.id!!,
                participant = otherUser?.let { toParticipantDto(it) },
                lastMessage = lastMessage,
                unreadCount = unreadCount,
                status = room.status.name
            )
        }
    }

    @Transactional
    override fun markAsRead(chatRoomId: UUID) {
        val authDetails = jwtConfigService.getAuthDetails()
        val chatRoom = chatRoomRepository.findById(chatRoomId)
            .orElseThrow { NotFoundException("Chat room not found") }

        if (chatRoom.participantOne.id != authDetails.userId && chatRoom.participantTwo?.id != authDetails.userId) {
            throw NotFoundException("Chat room not found")
        }

        chatMessageRepository.markMessagesAsRead(chatRoomId, authDetails.userId)

        // Push read-receipts back to the original sender for every message just marked READ
        val otherParticipant = if (chatRoom.participantOne.id == authDetails.userId)
            chatRoom.participantTwo else chatRoom.participantOne

        if (otherParticipant != null) {
            chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId)
                .filter { it.sender.id == otherParticipant.id && it.isRead }
                .forEach { msg ->
                    val update = MessageStatusUpdate(
                        messageId = msg.id!!,
                        chatRoomId = chatRoomId,
                        status = MessageStatus.READ
                    )
                    messagingTemplate.convertAndSendToUser(
                        otherParticipant.id.toString(), "/queue/status", update
                    )
                }
        }
    }

    override fun getOpenRooms(): List<ChatRoomDto> {
        return chatRoomRepository.findOpenRooms().map { room ->
            val messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(room.id!!)
            val lastMessage = messages.lastOrNull()?.let { toMessageDto(it) }
            val unreadCount = messages.count { !it.isRead }.toLong()

            ChatRoomDto(
                id = room.id!!,
                participant = toParticipantDto(room.participantOne),
                lastMessage = lastMessage,
                unreadCount = unreadCount,
                status = room.status.name
            )
        }
    }

    @Transactional
    override fun claimRoom(chatRoomId: UUID): ChatRoomDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val counsellor = userService.getUserById(authDetails.userId)

        if (counsellor.roles.none { it.name == "Administrator" }) {
            throw BadRequestException("Only counsellors can claim a chat room")
        }

        val room = chatRoomRepository.findById(chatRoomId)
            .orElseThrow { NotFoundException("Chat room not found") }

        if (room.status != ChatRoomStatus.OPEN) {
            throw BadRequestException("This chat room is no longer available for claiming")
        }

        room.participantTwo = counsellor
        room.status = ChatRoomStatus.ACTIVE
        val savedRoom = chatRoomRepository.save(room)

        messagingTemplate.convertAndSendToUser(
            savedRoom.participantOne.id.toString(),
            "/queue/messages",
            mapOf("type" to "COUNSELLOR_JOINED", "chatRoomId" to savedRoom.id.toString())
        )

        val messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(savedRoom.id!!)
        val lastMessage = messages.lastOrNull()?.let { toMessageDto(it) }
        val unreadCount = chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(
            savedRoom.id!!, authDetails.userId
        )

        return ChatRoomDto(
            id = savedRoom.id!!,
            participant = toParticipantDto(savedRoom.participantOne),
            lastMessage = lastMessage,
            unreadCount = unreadCount,
            status = savedRoom.status.name
        )
    }

    @Transactional
    override fun markDelivered(messageId: UUID) {
        val message = chatMessageRepository.findMessageById(messageId) ?: return
        if (message.messageStatus == MessageStatus.SENT) {
            chatMessageRepository.updateMessageStatus(messageId, MessageStatus.DELIVERED)
            val update = MessageStatusUpdate(
                messageId = messageId,
                chatRoomId = message.chatRoom.id!!,
                status = MessageStatus.DELIVERED
            )
            // Notify original sender
            messagingTemplate.convertAndSendToUser(
                message.sender.id.toString(), "/queue/status", update
            )
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun resolveRoom(request: SendMessageRequest, sender: UserModel): ChatRoomModel {
        return when {
            request.chatRoomId != null -> {
                val room = chatRoomRepository.findById(request.chatRoomId)
                    .orElseThrow { NotFoundException("Chat room not found") }
                val isCounsellor = sender.roles.any { it.name == "Administrator" }
                val isParticipant = room.participantOne.id == sender.id || room.participantTwo?.id == sender.id
                if (!isParticipant && !isCounsellor) {
                    throw BadRequestException("You are not a participant of this chat room")
                }
                if (isCounsellor && room.status == ChatRoomStatus.OPEN) {
                    room.participantTwo = sender
                    room.status = ChatRoomStatus.ACTIVE
                    chatRoomRepository.save(room)
                } else room
            }

            request.recipientId != null -> {
                val recipient = userService.getUserById(request.recipientId)
                if (sender.id == recipient.id) throw BadRequestException("Cannot send a message to yourself")
                chatRoomRepository.findByParticipants(sender.id!!, recipient.id!!)
                    ?: chatRoomRepository.save(
                        ChatRoomModel(participantOne = sender, participantTwo = recipient, status = ChatRoomStatus.ACTIVE)
                    )
            }

            else -> {
                chatRoomRepository.findByParticipantOneIdAndStatus(sender.id!!, ChatRoomStatus.OPEN)
                    ?: chatRoomRepository.save(ChatRoomModel(participantOne = sender))
            }
        }
    }

    private fun dispatchMessage(room: ChatRoomModel, sender: UserModel, messageDto: ChatMessageDto) {
        val recipient = if (room.participantOne.id == sender.id) room.participantTwo else room.participantOne
        if (recipient != null) {
            messagingTemplate.convertAndSendToUser(recipient.id.toString(), "/queue/messages", messageDto)
        } else {
            messagingTemplate.convertAndSend("/topic/open-rooms", messageDto)
        }
    }

    private fun toMessageDto(message: ChatMessageModel) = ChatMessageDto(
        id = message.id!!,
        senderId = message.sender.id!!,
        senderEmail = message.sender.email,
        content = message.content,
        isRead = message.isRead,
        messageStatus = message.messageStatus,
        createdAt = message.createdAt
    )

    private fun toParticipantDto(user: UserModel) = ChatParticipantDto(
        userId = user.id!!,
        email = user.email,
        firstName = user.firstName,
        lastName = user.lastName,
        profileImageUrl = user.profileImageUrl
    )
}
