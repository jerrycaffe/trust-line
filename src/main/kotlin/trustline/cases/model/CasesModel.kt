package trustline.cases.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.dto.Status
import trustline.appuser.model.*
import trustline.institution.model.InstitutionModel
import trustline.institution.model.UnitModel
import java.time.LocalDateTime
import java.util.*

@Table(name = "cases")
@Entity
data class CasesModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_type_id")
    val incidentType: IncidentTypesModel,
    @Column(name = "date_of_incident")
    val dateOfIncident: LocalDateTime? = LocalDateTime.now(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    val institution: InstitutionModel,
    @Column(name = "location")
    val location: String,
    @Column(name = "description")
    val description: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    val user: UserModel,
    @Column(name = "status")
    val caseStatus: Status,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_unit")
    val currentUnit: UnitModel,
    @Column(name = "deleted")
    val deleted: Boolean? = false
) : AuditModel()
