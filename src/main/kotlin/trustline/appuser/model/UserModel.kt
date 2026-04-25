package trustline.appuser.model;

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import trustline.appuser.dto.AuthProvider
import trustline.appuser.dto.Gender
import trustline.appuser.dto.Status
import trustline.institution.model.InstitutionModel
import java.util.*

@Entity
@Table(name = "users")
data class UserModel(
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
    @Column(name = "password", nullable = false)
    var password: String? = null,

    var profileImageUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    val institution: InstitutionModel,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<RoleModel> = mutableSetOf()

) : AuditModel()

data class UserResponseDto(
    val otpId: UUID? = null,
    val userId: UUID,
    val email: String,
    val phoneNumber: String?,
    val firstName: String?,
    val lastName: String?,
    val gender: Gender?,
    val status: Status?,
    val isAccountVerified: Boolean

)

fun UserModel.toResponseDto(otpId: UUID? = null) = UserResponseDto(
    otpId,
    userId = id!!,
    email,
    phoneNumber,
    firstName,
    lastName,
    gender,
    status,
    isAccountVerified
)

data class ProfileResponseDto(
    val userId: UUID,
    val email: String,
    val phoneNumber: String?,
    val firstName: String?,
    val lastName: String?,
    val gender: Gender?,
    val profileImageUrl: String?
)

fun UserModel.toProfileResponse() = ProfileResponseDto(
    userId = id!!,
    email = email,
    phoneNumber = phoneNumber,
    firstName = firstName,
    lastName = lastName,
    gender = gender,
    profileImageUrl = profileImageUrl
)
