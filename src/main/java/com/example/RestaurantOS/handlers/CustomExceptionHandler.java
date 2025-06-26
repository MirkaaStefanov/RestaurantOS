package com.example.RestaurantOS.handlers;

import com.example.RestaurantOS.exceptions.common.AccessDeniedException;
import com.example.RestaurantOS.exceptions.common.ApiException;
import com.example.RestaurantOS.exceptions.common.InternalServerErrorException;
import com.example.RestaurantOS.exceptions.common.TooManyRequestsException;
import com.example.RestaurantOS.exceptions.common.ValidationException;
import com.example.RestaurantOS.exceptions.user.UserLoginException;
import com.example.RestaurantOS.models.dto.response.ExceptionResponse;
import com.example.RestaurantOS.services.ExceptionService;
import com.example.RestaurantOS.utils.ApiExceptionParser;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for handling various types of exceptions and converting them into standardized API responses.
 */
@ControllerAdvice
@AllArgsConstructor
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    private final ExceptionService exceptionService;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeExceptions(RuntimeException exception) {
        return handleApiExceptions(new InternalServerErrorException(exception));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ExceptionResponse> handleInternalAuthServiceExceptions(InternalAuthenticationServiceException exception) {
        Throwable cause = exception.getCause();

        if (cause instanceof ApiException) {
            return handleApiExceptions((ApiException) cause);
        }

        return handleRuntimeExceptions(exception);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleBadCredentialsExceptions() {
        return handleApiExceptions(new UserLoginException());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedExceptions() {
        return handleApiExceptions(new AccessDeniedException());
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionExceptions(TransactionException exception) {
        if (exception.getRootCause() instanceof ConstraintViolationException) {
            return handleConstraintValidationExceptions((ConstraintViolationException) exception.getRootCause());
        }

        return handleRuntimeExceptions(exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintValidationExceptions(ConstraintViolationException exception) {
        return handleApiExceptions(new ValidationException(exception.getConstraintViolations()));
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ExceptionResponse> handleRateLimitException() {
        return handleApiExceptions(new TooManyRequestsException());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionResponse> handleApiExceptions(ApiException exception) {
        ExceptionResponse apiException = ApiExceptionParser.parseException(exception);

        if (exception instanceof InternalServerErrorException internalException) {
            exceptionService.log(internalException.getInnerRuntimeException(), internalException.getStatusCode());
        } else {
            exceptionService.log(exception);
        }

        return ResponseEntity
                .status(apiException.getStatus())
                .body(apiException);
    }
}
