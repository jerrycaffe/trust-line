package trustline.config.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import trustline.appuser.dto.TrustlineResponse
import java.io.IOException
import java.security.SignatureException

@Component
@Order(1)
class JwtRequestFilter(
    private val userSvc: SecurityUserService,
    private val jwtConfigService: JWTConfigService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtRequestFilter::class.java)


    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val tokenAndItsType = request.getHeader("Authorization")

        if (tokenAndItsType.isNullOrBlank() || !tokenAndItsType.startsWith("Bearer ")) {
            logger.debug("Authorization header is empty or missing bearer token")
            filterChain.doFilter(request, response)
            return
        }

        val token = tokenAndItsType.removePrefix("Bearer ").trim()
        val username: String = try {
            jwtConfigService.extractEmail(token)
        } catch (e: ExpiredJwtException) {
            logger.warn("JWT token expired: ${e.message}")
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token has expired, please login again")
            return
        } catch (e: SignatureException) {
            logger.warn("JWT signature validation failed: ${e.message}")
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token")
            return
        }

        if (username.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {
            val userDetails: UserDetails = userSvc.loadUserByUsername(username)

            try {
                val claims = jwtConfigService.extractClaims(token)
                val userId = claims["id"] as String
                val institutionId = claims["institution_id"] as String

                val grantedAuth = (claims["roles"] as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.map { SimpleGrantedAuthority(it) }
                    ?.toList()
                    ?: emptyList()

                if (jwtConfigService.isTokenValid(token, userDetails)) {

                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails, null, grantedAuth
                    ).apply {
                        details = mapOf(
                            "userId" to userId,
                            "institutionId" to institutionId,
                            "userName" to userDetails.username
                        )

                    }
                    SecurityContextHolder.getContext().authentication = authToken

                }
            } catch (e: Exception) {
                throw IllegalStateException(e.message)
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun writeErrorResponse(response: HttpServletResponse, status: HttpStatus, message: String) {
        response.status = status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(
            response.writer,
            TrustlineResponse<Any>(message = message, success = false, code = "01")
        )
    }

}