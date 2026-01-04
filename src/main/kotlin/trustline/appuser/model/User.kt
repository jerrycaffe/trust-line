package trustline.appuser.model;

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.dto.AuthProvider
import trustline.appuser.dto.Gender
import trustline.appuser.dto.Status
import java.util.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Enumerated(EnumType.STRING)
    var authProvider: AuthProvider,

    var googleId: String? = null,

    var phoneNumber: String? = null,
    @Column(name = "deleted")
    var isDeleted: Boolean = false,

    var firstName: String? = null,

    var lastName: String? = null,

    @Enumerated(EnumType.STRING)
    var gender: Gender? = null,

    @Enumerated(EnumType.STRING)
    var status: Status? = null,

    @Column(name = "accountVerified")
    var isAccountVerified: Boolean = false,

    var password: String,

    var profileImageUrl: String? = null,

    @ManyToMany
    @JoinTable(
        name = "users_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf()

) : AuditModel()
