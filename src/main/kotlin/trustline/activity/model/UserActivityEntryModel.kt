package trustline.activity.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import trustline.appuser.model.UserModel
import trustline.institution.model.InstitutionModel
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "user_activity_entries")
data class UserActivityEntryModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    val activity: ActivityModel,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserModel,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    val institution: InstitutionModel,

    @Column(name = "value", nullable = false, precision = 15, scale = 4)
    var value: BigDecimal,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null
) : AuditModel()
