package trustline.config

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import trustline.appuser.repository.UserRepository
import trustline.config.security.JWTConfigService
import java.security.Principal

@Component
class WebSocketAuthInterceptor(
    private val jwtConfigService: JWTConfigService,
    private val userRepository: UserRepository
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        if (accessor != null && StompCommand.CONNECT == accessor.command) {
            val authHeader = accessor.getFirstNativeHeader("Authorization")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)
                try {
                    val email = jwtConfigService.extractEmail(token)
                    if (!jwtConfigService.isTokenExpired(token)) {
                        val user = userRepository.findByEmail(email)
                        if (user.isPresent) {
                            val principal = StompPrincipal(user.get().id.toString())
                            accessor.user = principal
                        }
                    }
                } catch (_: Exception) {
                    // Invalid token — connection will proceed without principal
                }
            }
        }

        return message
    }
}

class StompPrincipal(private val userId: String) : Principal {
    override fun getName(): String = userId
}
