package trustline.notification.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import trustline.notification.dto.CreateNotificationRequest
import trustline.notification.dto.NotificationResponseDto
import trustline.notification.service.NotificationService
import trustline.config.security.PermissionAuthorities.ADMINISTRATOR
import trustline.config.security.PermissionAuthorities.MANAGE_NOTIFICATIONS
import java.util.*

@RestController
@RequestMapping("api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @PostMapping
    @PreAuthorize("hasAnyAuthority('$ADMINISTRATOR', '$MANAGE_NOTIFICATIONS')")
    fun create(@Validated @RequestBody request: CreateNotificationRequest): NotificationResponseDto {
        return notificationService.createNotification(request)
    }

    @GetMapping
    fun getMyNotifications(): List<NotificationResponseDto> {
        return notificationService.getMyNotifications()
    }

    @PutMapping("/{id}/read")
    fun markAsRead(@PathVariable id: UUID): NotificationResponseDto {
        return notificationService.markAsRead(id)
    }
}
