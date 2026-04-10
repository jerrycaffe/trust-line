package trustline.notification.model;

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.dto.OtpModeEnum
import trustline.appuser.dto.VerificationType
import trustline.appuser.model.AuditModel
import java.util.*


@Entity
@Table(name = "verifications")
data class VerificationModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    var messageId: String? = null,
    var userId: UUID? = null,
    var pin: String? = null,
    @Enumerated(EnumType.STRING)
    var mode: OtpModeEnum? = null,
    @Enumerated(EnumType.STRING)
    var type: VerificationType? = null
) : AuditModel()
