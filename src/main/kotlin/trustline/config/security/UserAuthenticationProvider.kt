package trustline.config.security;

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import trustline.appuser.repository.UserRepository

@Component
class UserAuthenticationProvider(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder
) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        val email = authentication.name
        val password = authentication.credentials as String

        val user = userRepository.findByEmail(email)
                .orElseThrow { UsernameNotFoundException("User not found") }

        if (!passwordEncoder.matches(password, user.password)) {
            throw BadCredentialsException("Invalid Credentials")
        }

        val authorities = user.roles
                ?.flatMap { role ->
                buildList {
            add(SimpleGrantedAuthority(role.name))
            role.permissions?.forEach {
                add(SimpleGrantedAuthority(it.name))
            }
        }
        }
            ?: emptyList()

        return UsernamePasswordAuthenticationToken(
                user.email,
                null,
                authorities
        )
    }

    override fun supports(authentication: Class<*>): Boolean =
            UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
}
