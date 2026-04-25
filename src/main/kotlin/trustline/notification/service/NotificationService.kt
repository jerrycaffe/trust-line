package trustline.notification.service

import trustline.appuser.model.UserModel
import trustline.notification.dto.CreateNotificationRequest
import trustline.notification.dto.NotificationResponseDto
import java.util.*

interface NotificationService {
    fun createNotification(request: CreateNotificationRequest): NotificationResponseDto
    fun createInternalNotification(topic: String, message: String, user: UserModel)
    fun getMyNotifications(): List<NotificationResponseDto>
    fun markAsRead(notificationId: UUID): NotificationResponseDto
}
