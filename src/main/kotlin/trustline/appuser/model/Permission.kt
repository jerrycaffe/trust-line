package trustline.appuser.model;

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*


@Entity
@Table(name = "permissions")
data class Permission(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    val name: String,
    val description: String,

    @ManyToMany(mappedBy = "permissions")
    val roles: Set<Role>? = null
) : AuditModel()