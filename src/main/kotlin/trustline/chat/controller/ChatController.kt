package trustline.chat.controller

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import trustline.chat.dto.ChatMessageDto
import trustline.chat.dto.ChatRoomDto
import trustline.chat.dto.SendMessageRequest
import trustline.chat.service.ChatService
import java.util.*

/**
 * REST controller for the chat system.
 *
 * All endpoints require a valid JWT (`Authorization: Bearer <token>`).
 * Base path: `api/v1/chat`
 *
 * Heavy lifting is delegated to [ChatService]; this controller is a pure
 * translation layer between HTTP and the service API.
 */
@RestController
@RequestMapping("api/v1/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping("/send")
    fun sendMessage(@Validated @RequestBody request: SendMessageRequest): ChatMessageDto {
        return chatService.sendMessage(request)
    }

    @GetMapping("/rooms")
    fun getMyChatRooms(): List<ChatRoomDto> {
        return chatService.getMyChatRooms()
    }

    /** Counsellor dashboard: lists all OPEN rooms waiting for a counsellor */
    @GetMapping("/rooms/open")
    fun getOpenRooms(): List<ChatRoomDto> {
        return chatService.getOpenRooms()
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    fun getChatMessages(@PathVariable chatRoomId: UUID): List<ChatMessageDto> {
        return chatService.getChatMessages(chatRoomId)
    }

    @PutMapping("/rooms/{chatRoomId}/read")
    fun markAsRead(@PathVariable chatRoomId: UUID) {
        chatService.markAsRead(chatRoomId)
    }

    /** Counsellor takes ownership of an OPEN room and begins the conversation */
    @PostMapping("/rooms/{chatRoomId}/claim")
    fun claimRoom(@PathVariable chatRoomId: UUID): ChatRoomDto {
        return chatService.claimRoom(chatRoomId)
    }
}
