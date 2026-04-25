package trustline.config.security

import io.jsonwebtoken.Claims
import org.springframework.security.core.userdetails.UserDetails
import trustline.appuser.model.UserModel

interface JWTConfigService {
    fun generateToken(user: UserModel, roleNames: List<String>): String
    fun extractEmail(token: String): String
    fun extractClaims(token: String): Claims
    fun isTokenValid(token: String, userDetails: UserDetails): Boolean
    fun isTokenExpired(token: String): Boolean
    fun getAuthDetails(): AuthDetailsResponse
}