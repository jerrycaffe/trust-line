package trustline.notification.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import trustline.appuser.model.UserModel
import java.util.*

@Entity
@Table(name = "notifications")
data class NotificationsModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @Column(name = "topic")
    val topic: String,
    @Column(name = "message")
    val message: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    val user: UserModel
) : AuditModel()
