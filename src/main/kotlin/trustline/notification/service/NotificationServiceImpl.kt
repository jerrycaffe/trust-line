package trustline.notification.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import trustline.appuser.model.UserModel
import trustline.appuser.service.UserService
import trustline.config.exception.NotFoundException
import trustline.config.security.JWTConfigService
import trustline.notification.dto.CreateNotificationRequest
import trustline.notification.dto.NotificationResponseDto
import trustline.notification.model.NotificationsModel
import trustline.notification.repository.NotificationRepository
import java.util.*

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val userService: UserService,
    private val jwtConfigService: JWTConfigService
) : NotificationService {

    @Transactional
    override fun createNotification(request: CreateNotificationRequest): NotificationResponseDto {
        val user = userService.getUserById(request.userId!!)
        val notification = notificationRepository.save(
            NotificationsModel(
                topic = request.topic!!,
                message = request.message!!,
                user = user
            )
        )
        return toResponse(notification)
    }

    @Transactional
    override fun createInternalNotification(topic: String, message: String, user: UserModel) {
        notificationRepository.save(
            NotificationsModel(
                topic = topic,
                message = message,
                user = user
            )
        )
    }

    override fun getMyNotifications(): List<NotificationResponseDto> {
        val authDetails = jwtConfigService.getAuthDetails()
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(authDetails.userId)
            .map { toResponse(it) }
    }

    @Transactional
    override fun markAsRead(notificationId: UUID): NotificationResponseDto {
        val authDetails = jwtConfigService.getAuthDetails()
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NotFoundException("Notification not found") }

        if (notification.user.id != authDetails.userId) {
            throw NotFoundException("Notification not found")
        }

        notification.isRead = true
        notificationRepository.save(notification)
        return toResponse(notification)
    }

    private fun toResponse(notification: NotificationsModel) = NotificationResponseDto(
        id = notification.id!!,
        topic = notification.topic,
        message = notification.message,
        isRead = notification.isRead,
        createdAt = notification.createdAt
    )
}
