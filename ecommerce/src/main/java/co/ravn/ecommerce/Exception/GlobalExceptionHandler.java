package co.ravn.ecommerce.Exception;

import co.ravn.ecommerce.DTO.Response.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String GENERIC_ERROR_MESSAGE = "An unexpected error occurred. Please try again later.";
    private static final String NOT_FOUND_MESSAGE = "The requested resource was not found.";
    private static final String VALIDATION_FAILED_MESSAGE = "One or more fields have validation errors.";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneric(Exception exception, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        log.error("Unhandled exception [path={}, method={}]: {}", path,
                request != null ? request.getMethod() : "?",
                exception.getMessage(), exception);

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                GENERIC_ERROR_MESSAGE,
                path
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleBadCredentials(BadCredentialsException exception, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        log.warn("Bad credentials [path={}]: {}", path, exception.getMessage());

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                exception.getMessage() != null && !exception.getMessage().isEmpty()
                        ? exception.getMessage()
                        : "Invalid username or password",
                path
        );
        return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        log.warn("Resource not found [path={}]: {}", path, exception.getMessage());

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                NOT_FOUND_MESSAGE,
                path
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequest(BadRequestException exception, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        log.warn("Bad request [path={}]: {}", path, exception.getMessage());

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(),
                path
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        String errorDetails = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed [path={}]: {}", path, errorDetails);

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                VALIDATION_FAILED_MESSAGE,
                path
        );

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionResponse> handleConflict(ConflictException exception, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        log.warn("Conflict [path={}]: {}", path, exception.getMessage());

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                path
        );
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PaymentFailureException.class)
    public ResponseEntity<ExceptionResponse> handlePaymentFailure(PaymentFailureException exception, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        log.warn("Payment failure [path={}]: {}", path, exception.getMessage());

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_GATEWAY.value(),
                HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                exception.getMessage(),
                path
        );
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(InternalServiceException.class)
    public ResponseEntity<ExceptionResponse> handleInternalService(InternalServiceException exception, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        log.error("Internal service error [path={}]: {}", path, exception.getMessage(), exception);

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                exception.getMessage(),
                path
        );
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<ExceptionResponse> handleConfiguration(ConfigurationException exception, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "";
        log.error("Configuration error [path={}]: {}", path, exception.getMessage());

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                exception.getMessage(),
                path
        );
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
