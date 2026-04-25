package trustline.notification.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.notification.model.NotificationsModel
import java.util.*

@Repository
interface NotificationRepository : JpaRepository<NotificationsModel, UUID> {
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<NotificationsModel>
}
