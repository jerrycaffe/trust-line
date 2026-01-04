package trustline.config.security;

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Component
import trustline.appuser.repository.UserRepository

@Component
class GoogleAuthProvider(
    private val userRepository: UserRepository,
    private val jwtConfig: JWTConfig
) {

    fun authenticateWithGoogle(googleId: String): String {
        val user = userRepository.findByGoogleId(googleId)
            .orElseThrow { BadCredentialsException("Invalid Google ID") }

        return jwtConfig.generateToken(user)
    }
}

