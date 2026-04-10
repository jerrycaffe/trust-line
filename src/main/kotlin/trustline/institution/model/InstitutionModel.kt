package trustline.institution.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import java.util.*

@Entity
@Table(name = "institutions")
data class InstitutionModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @Column(name = "name")
    var name: String? = null,
    @Column(name = "contact_number")
    val phoneNumber: String? = null,
    @Column(name = "address")
    val address: String? = null,
    @Column(name = "logo_url")
    val logoUrl: String? = null
) : AuditModel()
