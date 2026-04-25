package trustline.config.security;

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import trustline.appuser.model.UserModel
import java.util.*
import javax.crypto.SecretKey

@Service
class JWTConfigServiceImple(
    @Value("\${jwt.signin.secret}")
    private val secret: String
) : JWTConfigService {


    private fun setKey(): SecretKey =
        Keys.hmacShaKeyFor(secret.toByteArray())


    override fun generateToken(user: UserModel, roleNames: List<String>): String =
        Jwts.builder()
            .setSubject(user.email)
            .claim("roles", roleNames)
            .claim("status", user.status)
            .claim("id", user.id)
            .claim("institution_id", user.institution.id)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 60 * 60 * 60 * 30))
            .signWith(setKey(), SignatureAlgorithm.HS512)
            .compact()

    override fun extractEmail(token: String): String =
        extractClaims(token).subject

    override fun extractClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(setKey())
            .build()
            .parseClaimsJws(token)
            .body

    override fun isTokenValid(token: String, userDetails: UserDetails): Boolean =
        extractEmail(token) == userDetails.username && !isTokenExpired(token)

    override fun isTokenExpired(token: String): Boolean =
        extractClaims(token).expiration.before(Date(System.currentTimeMillis()))

    override fun getAuthDetails(): AuthDetailsResponse {
        val auth = SecurityContextHolder.getContext().authentication
        val details = auth.details as Map<*, *>
        val userId = UUID.fromString(details["userId"] as String)
        val userName = details["userName"] as String
        val institutionId = UUID.fromString(details["institutionId"] as String)
        return AuthDetailsResponse(userId, institutionId, userName)
    }
}

data class AuthDetailsResponse(
    val userId: UUID,
    val institutionId: UUID,
    val userName: String
)
