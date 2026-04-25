package trustline.cases.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import trustline.appuser.model.UserModel
import trustline.institution.model.InstitutionModel
import java.util.*

@Entity
@Table(name = "incident_types")
data class IncidentTypesModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @Column(name = "name")
    var name: String,
    @Column(name = "description")
    var description: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    val institution: InstitutionModel,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserModel,
    @Column(name = "steps")
    var steps: Int,
    @Column(name = "deleted")
    var deleted: Boolean? = false
) : AuditModel()

data class IncidentTypeResponseDto(
    val id: UUID,
    val name: String,
    val description: String,
    val createdBy: String,
    val steps: Int
)

fun IncidentTypesModel.toIncidentTypeResponse() = IncidentTypeResponseDto(
    id!!, name, description, createdBy = createdBy.email, steps
)