package trustline.cases.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import trustline.appuser.model.UserModel
import trustline.institution.model.InstitutionModel
import java.util.*

@Entity
@Table(name = "resources")
data class ResourceModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @Column(name = "name")
    var name: String,
    @Column(name = "contents", columnDefinition = "TEXT")
    var contents: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_type_id")
    val incidentType: IncidentTypesModel,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_upload_id")
    var fileUpload: FileUploadsModel? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserModel,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    val institution: InstitutionModel,
    @Column(name = "deleted")
    var deleted: Boolean = false
) : AuditModel()
