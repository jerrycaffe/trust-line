package trustline.cases.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import trustline.institution.model.InstitutionModel
import trustline.appuser.model.UserModel
import java.util.*

@Entity
@Table(name ="incident_types")
data class IncidentTypesModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @Column(name = "name")
    var name: String,
    @Column(name = "description")
    val description: String,
    @Column(name = "institution_id")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    val institution: InstitutionModel,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserModel,
    @Column(name = "steps")
    val steps: Int,
    @Column(name = "deleted")
    val deleted: Boolean? = false
) : AuditModel()
