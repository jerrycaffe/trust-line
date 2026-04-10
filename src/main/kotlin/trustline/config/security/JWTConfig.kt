package trustline.config.security;

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import trustline.appuser.model.UserModel
import java.util.*
import javax.crypto.SecretKey

@Service
class JWTConfig(
    @Value("\${jwt.signin.secret}")
    private val secret: String
) {


    private fun setKey(): SecretKey =
        Keys.secretKeyFor(SignatureAlgorithm.HS256)

    fun generateToken(user: UserModel): String =
        Jwts.builder()
            .setSubject(user.email)
            .claim("roles", user.roles)
            .claim("status", user.status)
            .claim("id", user.id)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 60 * 60 * 60 * 30))
            .signWith(setKey(), SignatureAlgorithm.HS256)
            .compact()

    fun extractEmail(token: String): String =
        extractClaims(token).subject

     fun extractClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.toByteArray()))
            .build()
            .parseClaimsJws(token)
            .body

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean =
        extractEmail(token) == userDetails.username && !isTokenExpired(token)

     fun isTokenExpired(token: String): Boolean =
        extractClaims(token).expiration.before(Date(System.currentTimeMillis()))
}
