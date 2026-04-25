package trustline.appuser.model;

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.institution.model.InstitutionModel
import java.util.*


/**
 * JPA entity representing a fine-grained permission (e.g. "VIEW_ALL_USERS").
 *
 * **Institution scoping:** a `null` [institution] means the permission is defined
 * globally (seeded by Flyway).  A non-null [institution] limits the permission to
 * that institution.  [PermissionRepository.findAllByInstitutionIdOrGlobal] always
 * includes both institution-specific and global permissions so administrators can
 * assign global permissions to their custom roles.
 *
 * Permissions become [SimpleGrantedAuthority] entries that sit alongside the role
 * name in the user's security context, enabling method-level security guards like
 * `@PreAuthorize("hasAuthority('VIEW_ALL_USERS')")`.
 */
@Entity
@Table(name = "permissions")
data class PermissionModel(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    val name: String,
    val description: String,

    @ManyToMany(mappedBy = "permissions")
    val roles: Set<RoleModel>? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    val institution: InstitutionModel? = null
) : AuditModel()