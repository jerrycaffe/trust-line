package trustline.cases.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import java.util.*

@Entity
@Table(name = "file_uploads")
data class FileUploadsModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @Column(name = "upload_url")
    val uploadUrl: String
) : AuditModel()
