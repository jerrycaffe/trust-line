package trustline.appuser.model;

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "permissions")
open class Role(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    open var id: UUID? = null,

    open var name: String? = null,

    open var description: String? = null,

    @ManyToMany(mappedBy = "roles")
    open var users: MutableSet<UserModel> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "roles_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
   open var permissions: MutableSet<Permission> = mutableSetOf()

) : AuditModel()
