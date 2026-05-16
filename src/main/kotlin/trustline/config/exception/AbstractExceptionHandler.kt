package trustline.config.exception;

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException
import trustline.appuser.dto.TrustlineResponse
import trustline.config.exception.ErrorCodes.BAD_CREDENTIALS_ERROR_CODE
import trustline.config.exception.ErrorCodes.FILE_UPLOAD_SIZE_EXCEEDED_ERROR_CODE
import trustline.config.exception.ErrorCodes.FILE_UPLOAD_SIZE_EXCEEDED_ERROR_MESSAGE
import trustline.config.exception.ErrorCodes.FORMAT_ERROR_CODE
import trustline.config.exception.ErrorCodes.ILLEGAL_INPUT_ERROR_CODE
import trustline.config.exception.ErrorCodes.ILLEGAL_INPUT_ERROR_MESSAGE
import trustline.config.exception.ErrorCodes.NOT_FOUND_ERROR_CODE
import trustline.config.exception.ErrorCodes.SERVER_ERROR_CODE
import trustline.config.exception.ErrorCodes.SERVER_ERROR_MESSAGE


@ControllerAdvice
class AbstractExceptionHandler {

    private val log = KotlinLogging.logger{}

    private fun errorResponse(code: String, message: String, status: HttpStatus): ResponseEntity<TrustlineResponse<Any>> =
        ResponseEntity(TrustlineResponse(message = message, success = false, code = code), status)

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<TrustlineResponse<Any>> {
        log.error{"General Exception >>> $ex"}
        return errorResponse(SERVER_ERROR_CODE, SERVER_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("Improper Argument passed >>>> $ex", ex)
        return errorResponse(ILLEGAL_INPUT_ERROR_CODE, ILLEGAL_INPUT_ERROR_MESSAGE, HttpStatus.NOT_ACCEPTABLE)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("BadRequestException >>> $ex", ex)
        return errorResponse(ex.code, ex.message ?: "Bad request", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DuplicateException::class)
    fun handleDuplicate(ex: DuplicateException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("Duplicate Exception >>> $ex", ex)
        return errorResponse(ex.code, ex.message ?: "Duplicate record", HttpStatus.CONFLICT)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("NotFoundException >>> $ex", ex)
        return errorResponse(ex.code, ex.message ?: "Not found", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgNotValid(ex: MethodArgumentNotValidException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("MethodArgumentNotValid Exception >>> $ex", ex)
        val errorMessage = ex.bindingResult.fieldError?.defaultMessage ?: "Validation failed"
        return errorResponse(FORMAT_ERROR_CODE, errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ServletRequestBindingException::class)
    fun handleServletRequestBinding(ex: ServletRequestBindingException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("ServletRequestBinding Exception >>> $ex", ex)
        return errorResponse(FORMAT_ERROR_CODE, ex.message ?: "Request binding error", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("HttpMediaTypeNotSupportedException >>> $ex", ex)
        return errorResponse(FORMAT_ERROR_CODE, ex.message ?: "Unsupported media type", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("HttpRequestMethodNotSupportedException >>> $ex", ex)
        return errorResponse(FORMAT_ERROR_CODE, ex.message ?: "Method not supported", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("AccessDeniedException >>> $ex", ex)
        return errorResponse(FORMAT_ERROR_CODE, "Access Denied", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDenied(ex: AuthorizationDeniedException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("AuthorizationDeniedException >>> $ex", ex)
        return errorResponse(FORMAT_ERROR_CODE, "Access Denied", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceeded(ex: MaxUploadSizeExceededException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("MaxUploadSizeExceededException >>> $ex", ex)
        return errorResponse(FILE_UPLOAD_SIZE_EXCEEDED_ERROR_CODE, FILE_UPLOAD_SIZE_EXCEEDED_ERROR_MESSAGE, HttpStatus.PAYLOAD_TOO_LARGE)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("HttpMessageNotReadableException >>> $ex", ex)
        return errorResponse(FORMAT_ERROR_CODE, "Incorrect input supplied to the system", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFound(ex: UsernameNotFoundException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("UsernameNotFoundException >>> $ex", ex)
        return errorResponse(NOT_FOUND_ERROR_CODE, ex.message ?: "User not found", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<TrustlineResponse<Any>> {
        log.error("BadCredentialsException >>> $ex", ex)
        return errorResponse(BAD_CREDENTIALS_ERROR_CODE, ex.message ?: "Invalid credentials", HttpStatus.BAD_REQUEST)
    }
}
