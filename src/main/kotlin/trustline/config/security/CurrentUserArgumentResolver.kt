package trustline.config.security

import org.springframework.core.MethodParameter
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID

class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java) &&
            parameter.parameterType == AuthDetailsResponse::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationCredentialsNotFoundException("Authentication not found")

        val details = authentication.details as? Map<*, *>
            ?: throw AuthenticationCredentialsNotFoundException("Authentication details not found")

        val userIdValue = details["userId"] as? String
            ?: throw AuthenticationCredentialsNotFoundException("Authenticated user id not found")
        val institutionIdValue = details["institutionId"] as? String
            ?: throw AuthenticationCredentialsNotFoundException("Authenticated institution id not found")
        val userName = details["userName"] as? String
            ?: throw AuthenticationCredentialsNotFoundException("Authenticated username not found")

        val userId = UUID.fromString(userIdValue)
        val institutionId = UUID.fromString(institutionIdValue)

        return AuthDetailsResponse(userId, institutionId, userName)
    }
}
