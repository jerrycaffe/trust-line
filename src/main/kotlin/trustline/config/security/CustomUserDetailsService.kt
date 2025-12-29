package trustline.config.security;

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import trustline.appuser.repository.UserRepository

/**
 *
 *
 * @author Adeleye Jeremiah
 * @since 1.0.0
 * */

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Transactional
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow {
                UsernameNotFoundException("User not found with this email : $email")
            }

        return PrincipalUser(user)
    }
}