package trustline.cases.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import java.util.*

@Entity
@Table(name = "case_file_uploads")
data class CaseFileUploadsModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_upload_id")
    val fileUpload: FileUploadsModel,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    val case: CasesModel? = null
) : AuditModel()
