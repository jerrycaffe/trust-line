package trustline.appuser.model;

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.dto.OtpModeEnum
import trustline.appuser.dto.VerificationType
import java.util.*


@Entity
@Table(name = "verifications")
data class VerificationModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    val messageId: String? = null,
    val userId: UUID? = null,
    val pin: String? = null,
    @Enumerated(EnumType.STRING)
    val mode: OtpModeEnum? = null,
    @Enumerated(EnumType.STRING)
    val type: VerificationType? = null
) : AuditModel()
