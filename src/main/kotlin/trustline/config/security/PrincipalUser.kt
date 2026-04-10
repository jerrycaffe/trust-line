package trustline.config.security;

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import trustline.appuser.dto.AuthProvider
import trustline.appuser.model.Role
import trustline.appuser.model.UserModel
import java.util.*

class PrincipalUser(user: UserModel) : UserDetails {

    private val authorities: MutableSet<GrantedAuthority> = HashSet()

    val id: UUID = user.id!!
    val authProvider: AuthProvider = user.authProvider
    private val username: String = user.email
    private val enabled: Boolean = !user.isDeleted && user.isAccountVerified
    private val password: String = user.password!!

    init {
        setAuthorities(user.roles)
    }

    private fun setAuthorities(roles: Set<Role>?) {
        roles ?: return

        for (role in roles) {
            authorities.add(SimpleGrantedAuthority(role.name))

            role.permissions
                .map { SimpleGrantedAuthority(it.name) }
                .forEach(authorities::add)
        }
    }

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = enabled
}
