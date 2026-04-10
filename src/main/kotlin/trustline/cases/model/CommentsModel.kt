package trustline.cases.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.model.AuditModel
import trustline.appuser.model.UserModel
import java.util.*

@Entity
@Table(name = "comments")
data class CommentsModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,
    @Column(name = "comment")
    val comment: String,
    @OneToOne
    @JoinColumn(name = "case_id")
    val case: CasesModel,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter")
    val commenter: UserModel
) : AuditModel()
