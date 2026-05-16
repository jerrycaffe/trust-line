package trustline.appuser.model;

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.institution.model.InstitutionModel
import java.util.*

/**
 * JPA entity representing a role that can be assigned to users.
 *
 * **Institution scoping:** a `null` [institution] means the role is *global*
 * (seeded by Flyway and shared across all institutions, e.g. "User", "Administrator").
 * A non-null [institution] restricts the role to that institution only.
 *
 * Authorities exposed to Spring Security combine the **role name** with every
 * **permission name** attached via the `roles_permissions` join table, so both
 * `@PreAuthorize("hasAuthority('Administrator')")` and permission-level guards work.
 */
@Entity
@Table(name = "roles")
data class RoleModel(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    val id: UUID? = null,

    var name: String? = null,

    var description: String? = null,

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    var users: MutableSet<UserModel> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "roles_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    var permissions: MutableSet<PermissionModel> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    var institution: InstitutionModel? = null,

    @Column(name = "institution_id", insertable = false, updatable = false)
    var institutionId: UUID? = null

) : AuditModel() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RoleModel) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
