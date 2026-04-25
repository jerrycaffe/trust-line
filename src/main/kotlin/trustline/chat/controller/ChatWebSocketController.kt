package trustline.chat.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import trustline.chat.dto.*
import trustline.chat.model.ChatMessageModel
import trustline.chat.model.ChatRoomModel
import trustline.chat.model.ChatRoomStatus
import trustline.chat.model.MessageStatus
import trustline.chat.repository.ChatMessageRepository
import trustline.chat.repository.ChatRoomRepository
import trustline.chat.service.PresenceService
import trustline.appuser.model.UserModel
import trustline.appuser.service.UserService
import trustline.config.exception.BadRequestException
import java.security.Principal
import java.util.*

@Controller
class ChatWebSocketController(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userService: UserService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val presenceService: PresenceService
) {

    /** Send a message. Recipient receives it on /user/{id}/queue/messages */
    @MessageMapping("/chat.send")
    fun handleSendMessage(@Payload request: SendMessageRequest, principal: Principal) {
        val sender = senderFrom(principal)
        val chatRoom = resolveRoom(request, sender)

        val recipient = if (chatRoom.participantOne.id == sender.id) chatRoom.participantTwo else chatRoom.participantOne
        val initialStatus = if (recipient != null && presenceService.isOnline(recipient.id!!))
            MessageStatus.DELIVERED else MessageStatus.SENT

        val message = chatMessageRepository.save(
            ChatMessageModel(chatRoom = chatRoom, sender = sender, content = request.content!!, messageStatus = initialStatus)
        )

        val messageDto = ChatMessageDto(
            id = message.id!!,
            senderId = message.sender.id!!,
            senderEmail = message.sender.email,
            content = message.content,
            isRead = message.isRead,
            messageStatus = message.messageStatus,
            createdAt = message.createdAt
        )

        if (recipient != null) {
            messagingTemplate.convertAndSendToUser(recipient.id.toString(), "/queue/messages", messageDto)
        } else {
            messagingTemplate.convertAndSend("/topic/open-rooms", messageDto)
        }

        // Echo confirmation back to sender
        messagingTemplate.convertAndSendToUser(sender.id.toString(), "/queue/messages", messageDto)
    }

    /**
     * Client sends this once it has displayed the message to confirm delivery.
     * Triggers a DELIVERED status push back to the original sender.
     */
    @MessageMapping("/chat.delivered")
    fun handleDelivered(@Payload ack: DeliveryAckRequest, principal: Principal) {
        val recipient = senderFrom(principal)   // the person who received and is ACKing
        val message = chatMessageRepository.findMessageById(ack.messageId) ?: return

        // Only update if not yet READ and the ACKing user is NOT the original sender
        if (message.sender.id == recipient.id) return
        if (message.messageStatus == MessageStatus.READ) return

        chatMessageRepository.updateMessageStatus(ack.messageId, MessageStatus.DELIVERED)

        val update = MessageStatusUpdate(
            messageId = ack.messageId,
            chatRoomId = message.chatRoom.id!!,
            status = MessageStatus.DELIVERED
        )
        messagingTemplate.convertAndSendToUser(message.sender.id.toString(), "/queue/status", update)
    }

    /**
     * Client sends while the user is composing.
     * Forwarded to the other participant as a TypingNotification.
     */
    @MessageMapping("/chat.typing")
    fun handleTyping(@Payload event: TypingEvent, principal: Principal) {
        val sender = senderFrom(principal)
        val room = chatRoomRepository.findById(event.chatRoomId).orElse(null) ?: return

        val recipient = if (room.participantOne.id == sender.id) room.participantTwo else room.participantOne
        recipient?.id?.let { recipientId ->
            val notification = TypingNotification(
                chatRoomId = event.chatRoomId,
                senderId = sender.id!!,
                typing = event.typing
            )
            messagingTemplate.convertAndSendToUser(recipientId.toString(), "/queue/typing", notification)
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun senderFrom(principal: Principal): UserModel {
        val senderId = UUID.fromString(principal.name)
        return userService.getUserById(senderId)
    }

    private fun resolveRoom(request: SendMessageRequest, sender: UserModel): ChatRoomModel {
        return when {
            request.chatRoomId != null -> {
                val room = chatRoomRepository.findById(request.chatRoomId)
                    .orElseThrow { BadRequestException("Chat room not found") }
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
}
