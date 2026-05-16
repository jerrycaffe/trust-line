package trustline.config.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import trustline.appuser.repository.UserRepository
import trustline.config.exception.NotFoundException
import java.util.UUID

@Service
class SecurityUserService(
    private val usersRepository: UserRepository
) : UserDetailsService {
    fun loadUserByEmailAndInstitutionId(email: String, institutionId: UUID): UserDetails {
        val dbUser = usersRepository.findWithAuthoritiesByEmailAndInstitutionId(email, institutionId)
            ?: throw NotFoundException("$email not found in institution")

        val authorities = dbUser.roles.flatMap { role ->
            buildList {
                role.name?.let { add(SimpleGrantedAuthority(it)) }
                role.permissions.forEach { add(SimpleGrantedAuthority(it.name)) }
            }
        }

        return User(
            dbUser.email,
            dbUser.password,
            !dbUser.isDeleted,
            true,
            true,
            true,
            authorities.distinctBy { it.authority }
        )
    }

    override fun loadUserByUsername(username: String): UserDetails {
        val candidates = usersRepository.findAllByEmail(username)
        val dbUser = candidates.firstOrNull()
            ?: throw NotFoundException("$username not found")
        return User(
            dbUser.email,
            dbUser.password,
            !dbUser.isDeleted,
            true,
            true,
            true,
            dbUser.roles.mapNotNull { it.name }.map { SimpleGrantedAuthority(it) }
        )
    }
}