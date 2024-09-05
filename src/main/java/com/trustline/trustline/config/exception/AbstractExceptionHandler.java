package com.trustline.trustline.config.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import static com.trustline.trustline.config.exception.ErrorCodes.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@ControllerAdvice
@Slf4j
public class AbstractExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> constraintViolation(Exception ex) {
        log.error("General Exception >>> " + ex);
        ex.printStackTrace();
        return new ResponseEntity<>(new Response(new Error(SERVER_ERROR_CODE, SERVER_ERROR_MESSAGE)), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> constraintViolation(IllegalArgumentException ex) {
        log.error("Improper Argument passed >>>> " + ex);
        ex.printStackTrace();
        return new ResponseEntity<>(new Response(new Error(ILLEGAL_INPUT_ERROR_CODE, ILLEGAL_INPUT_ERROR_MESSAGE)), NOT_ACCEPTABLE);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Response> constraintViolation(BadRequestException ex) {
        log.error("BadRequestException Exception >>> " + ex);
        return new ResponseEntity<>(new Response(ex.getError()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Response> constraintViolation(DuplicateException ex) {
        log.error("Duplicate Exception >>> " + ex);
        return new ResponseEntity<>(new Response(ex.getError()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response> constraintViolation(NotFoundException ex) {
        log.error("Duplicate Exception >>> " + ex);
        return new ResponseEntity<>(new Response(ex.getError()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> constraintViolation(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValid Exception >>> " + ex);
        return new ResponseEntity<>(new Response(new Error(FORMAT_ERROR_CODE, ex.getBindingResult()
                .getFieldError().getDefaultMessage())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Response> constraintViolation(ServletRequestBindingException ex) {
        log.error("ServletRequestBinding Exception >>> " + ex);
        return new ResponseEntity<>(new Response(new Error(FORMAT_ERROR_CODE, ex.getMessage())),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Response> constraintViolation(HttpMediaTypeNotSupportedException ex) {
        log.error("HttpMediaTypeNotSupportedException Exception >>> " + ex);
        return new ResponseEntity<>(new Response(new Error(FORMAT_ERROR_CODE, ex.getMessage())),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response> constraintViolation(HttpRequestMethodNotSupportedException ex) {
        log.error("HttpRequestMethodNotSupportedException Exception >>> " + ex);
        return new ResponseEntity<>(new Response(new Error(FORMAT_ERROR_CODE, ex.getMessage())),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> constraintViolation(AccessDeniedException ex) {
        log.error("AccessDeniedException Exception >>> " + ex);
        ex.printStackTrace();
        return new ResponseEntity<>(new Response(new Error(FORMAT_ERROR_CODE, ex.getMessage())),
                HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public  ResponseEntity<Response> constraintViolation(HttpMessageNotReadableException ex){
        log.error("HttpMessageNotReadableException Exception >>> "+ex);
        return new ResponseEntity<>(new Response(new Error(FORMAT_ERROR_CODE, "Incorrect input supplied to the system")), HttpStatus.BAD_REQUEST);
    }
}
