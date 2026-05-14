package uz.kapitalbank.pg.payflow.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import uz.kapitalbank.pg.payflow.dto.error.ErrorDto;
import uz.kapitalbank.pg.payflow.exception.ApplicationException;
import uz.kapitalbank.pg.payflow.exception.HttpClientException;
import uz.kapitalbank.pg.payflow.exception.HttpServerException;

import static uz.kapitalbank.pg.payflow.constant.error.Error.*;
import static uz.kapitalbank.pg.payflow.constant.error.ErrorType.*;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorDto> handleApplicationException(ApplicationException ex) {
        log.error("ApplicationException: {}", ex.getMessage());
        var error = ErrorDto.builder()
                .code(ex.getCode())
                .type(ex.getErrorType())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(HttpClientException.class)
    public ResponseEntity<ErrorDto> handleHttpClientException(HttpClientException ex) {
        log.error("HttpClientException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .code(HTTP_CLIENT_ERROR_CODE.getCode())
                .type(EXTERNAL)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(HttpServerException.class)
    public ResponseEntity<ErrorDto> handleHttpServerException(HttpServerException ex) {
        log.error("HttpServerException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .code(EXTERNAL_SERVICE_FAILED_ERROR_CODE.getCode())
                .type(EXTERNAL)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorDto> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        log.error("MissingRequestHeaderException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .type(INTERNAL)
                .code(MISSING_REQUEST_HEADER_ERROR_CODE.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorDto> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        log.error("HttpMediaTypeNotSupportedException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .type(INTERNAL)
                .code(METHOD_NOT_SUPPORTED_ERROR_CODE.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorDto> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("HttpRequestMethodNotSupportedException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .type(INTERNAL)
                .code(METHOD_NOT_SUPPORTED_ERROR_CODE.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorDto> handleResourceAccessException(ResourceAccessException ex) {
        log.error("ResourceAccessException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .type(INTERNAL)
                .code(INTERNAL_TIMEOUT_ERROR_CODE.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("MethodArgumentTypeMismatchException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .type(INTERNAL)
                .code(INVALID_REQUEST_PARAM_ERROR_CODE.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage(), ex);
        var validationErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .toList();
        var error = ErrorDto.builder()
                .type(VALIDATION)
                .code(VALIDATION_ERROR_CODE.getCode())
                .message(VALIDATION_ERROR_CODE.getMessage())
                .validationErrors(validationErrors)
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("ConstraintViolationException: {}", ex.getMessage(), ex);
        var validationErrors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        var error = ErrorDto.builder()
                .type(VALIDATION)
                .code(VALIDATION_ERROR_CODE.getCode())
                .message(VALIDATION_ERROR_CODE.getMessage())
                .validationErrors(validationErrors)
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .type(INTERNAL)
                .code(JSON_NOT_VALID_ERROR_CODE.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorDto> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.error("NoHandlerFoundException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .type(INTERNAL)
                .code(HANDLER_NOT_FOUND_ERROR_CODE.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleIllegalStateException(IllegalStateException ex) {
        log.error("IllegalStateException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .type(INTERNAL)
                .code(INTERNAL_SERVICE_ERROR_CODE.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .code(INTERNAL_SERVICE_ERROR_CODE.getCode())
                .type(INTERNAL)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        log.error("Exception: {}", ex.getMessage(), ex);
        var error = ErrorDto.builder()
                .code(INTERNAL_SERVICE_ERROR_CODE.getCode())
                .type(INTERNAL)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
