package trustline.config.exception;

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import trustline.config.exception.ErrorCodes.BAD_CREDENTIALS_ERROR_CODE
import trustline.config.exception.ErrorCodes.FORMAT_ERROR_CODE
import trustline.config.exception.ErrorCodes.ILLEGAL_INPUT_ERROR_CODE
import trustline.config.exception.ErrorCodes.ILLEGAL_INPUT_ERROR_MESSAGE
import trustline.config.exception.ErrorCodes.NOT_FOUND_ERROR_CODE
import trustline.config.exception.ErrorCodes.SERVER_ERROR_CODE
import trustline.config.exception.ErrorCodes.SERVER_ERROR_MESSAGE


@ControllerAdvice
class AbstractExceptionHandler {

    private val log = KotlinLogging.logger{}

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<Response> {
        log.error{"General Exception >>> $ex"}
        val errorCode = Error(SERVER_ERROR_CODE, SERVER_ERROR_MESSAGE)
        return ResponseEntity(Response(errorCode), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Response> {
        log.error("Improper Argument passed >>>> $ex", ex)
        return ResponseEntity(
            Response(Error(ILLEGAL_INPUT_ERROR_CODE, ILLEGAL_INPUT_ERROR_MESSAGE)),
            HttpStatus.NOT_ACCEPTABLE
        )
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<Response> {
        log.error("BadRequestException >>> $ex", ex)
        return ResponseEntity(Response(ex.error), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DuplicateException::class)
    fun handleDuplicate(ex: DuplicateException): ResponseEntity<Response> {
        log.error("Duplicate Exception >>> $ex", ex)
        return ResponseEntity(Response(ex.error), HttpStatus.CONFLICT)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<Response> {
        log.error("NotFoundException >>> $ex", ex)
        return ResponseEntity(Response(ex.error), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgNotValid(ex: MethodArgumentNotValidException): ResponseEntity<Response> {
        log.error("MethodArgumentNotValid Exception >>> $ex", ex)
        val errorMessage = ex.bindingResult.fieldError?.defaultMessage ?: "Validation failed"
        return ResponseEntity(Response(Error(FORMAT_ERROR_CODE, errorMessage)), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ServletRequestBindingException::class)
    fun handleServletRequestBinding(ex: ServletRequestBindingException): ResponseEntity<Response> {
        log.error("ServletRequestBinding Exception >>> $ex", ex)
        return ResponseEntity(Response(Error(FORMAT_ERROR_CODE, ex.message ?: "")), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResponseEntity<Response> {
        log.error("HttpMediaTypeNotSupportedException >>> $ex", ex)
        return ResponseEntity(Response(Error(FORMAT_ERROR_CODE, ex.message ?: "")), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<Response> {
        log.error("HttpRequestMethodNotSupportedException >>> $ex", ex)
        return ResponseEntity(Response(Error(FORMAT_ERROR_CODE, ex.message ?: "")), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<Response> {
        log.error("AccessDeniedException >>> $ex", ex)
        return ResponseEntity(Response(Error(FORMAT_ERROR_CODE, ex.message ?: "")), HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<Response> {
        log.error("HttpMessageNotReadableException >>> $ex", ex)
        return ResponseEntity(
            Response(Error(FORMAT_ERROR_CODE, "Incorrect input supplied to the system")),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFound(ex: UsernameNotFoundException): ResponseEntity<Response> {
        log.error("UsernameNotFoundException >>> $ex", ex)
        return ResponseEntity(Response(Error(NOT_FOUND_ERROR_CODE, ex.message ?: "")), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<Response> {
        log.error("BadCredentialsException >>> $ex", ex)
        return ResponseEntity(Response(Error(BAD_CREDENTIALS_ERROR_CODE, ex.message ?: "")), HttpStatus.BAD_REQUEST)
    }
}
