package trustline.config.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import trustline.appuser.repository.UserRepository
import trustline.config.exception.NotFoundException

@Service
class SecurityUserService(
    private val usersRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val dbUser = usersRepository.findByEmailOrPhoneNumber(username, username)
            ?: throw NotFoundException("$username not found")
        return User(
            dbUser.email,
            dbUser.password,
            !dbUser.isDeleted,
            true,
            true,
            true,
            dbUser.roles.map { SimpleGrantedAuthority(it.name) }
        )
    }
}