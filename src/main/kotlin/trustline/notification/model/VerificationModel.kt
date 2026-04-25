package trustline.notification.model;

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.dto.OtpModeEnum
import trustline.appuser.dto.Status
import trustline.appuser.dto.VerificationType
import trustline.appuser.model.AuditModel
import trustline.appuser.model.UserModel
import java.util.*


@Entity
@Table(name = "verifications")
data class VerificationModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var messageId: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserModel,
    var pin: String? = null,
    @Enumerated(EnumType.STRING)
    var mode: OtpModeEnum? = null,
    @Enumerated(EnumType.STRING)
    var type: VerificationType? = null,
    @Enumerated(EnumType.STRING)
    var status: Status
) : AuditModel()
