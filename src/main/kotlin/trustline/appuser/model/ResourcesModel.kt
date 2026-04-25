package trustline.appuser.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.cases.model.FileUploadsModel
import trustline.cases.model.IncidentTypesModel
import java.util.*

@Entity
@Table(name = "resources")
data class ResourcesModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @Column(name = "name")
    val name: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_type_id")
    val incidentType: IncidentTypesModel,
    @Column(name = "contents")
    val content: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_upload_id")
    val uploadedFile: FileUploadsModel,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val createdBy: UserModel,

    ) : AuditModel()
