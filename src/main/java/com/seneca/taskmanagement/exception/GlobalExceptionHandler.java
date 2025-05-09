package com.seneca.taskmanagement.exception;

import com.seneca.taskmanagement.util.LoggingUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        // Create structured log entry
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("statusCode", HttpStatus.NOT_FOUND.value());
        metadata.put("exceptionType", ex.getClass().getSimpleName());
        metadata.put("path", request.getRequestURI());
        
        LoggingUtils.logOperation(log, "Resource not found: " + ex.getMessage(), metadata);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource not found",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        log.error("Resource already exists: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Resource already exists",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, 
            HttpServletRequest request) {
        
        // Create structured log entry
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("statusCode", HttpStatus.BAD_REQUEST.value());
        metadata.put("exceptionType", ex.getClass().getSimpleName());
        metadata.put("path", request.getRequestURI());
        
        LoggingUtils.logOperation(log, "Bad request: " + ex.getMessage(), metadata);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad request",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Create structured log entry
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("statusCode", HttpStatus.BAD_REQUEST.value());
        metadata.put("exceptionType", ex.getClass().getSimpleName());
        metadata.put("path", request.getRequestURI());
        metadata.put("validationErrors", errors);
        
        LoggingUtils.logOperation(log, "Validation error", metadata);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                "Validation failed for request parameters",
                request.getRequestURI(),
                LocalDateTime.now(),
                errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Constraint violation: {}", errors);
        ValidationErrorResponse response = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                LocalDateTime.now(),
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, 
            HttpServletRequest request) {
        
        // Create structured log entry with stack trace
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        metadata.put("exceptionType", ex.getClass().getSimpleName());
        metadata.put("path", request.getRequestURI());
        
        // Add request ID to response for correlation
        String requestId = MDC.get("requestId");
        
        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "An unexpected error occurred",
                request.getRequestURI(),
                LocalDateTime.now());
                
        if (requestId != null) {
            errorResponse = errorResponse.addAdditionalInfo("requestId", requestId);
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public record ErrorResponse(int status, String message, String details, String path, LocalDateTime timestamp, Map<String, String> errors) {
        public ErrorResponse(int status, String message, String details, String path, LocalDateTime timestamp) {
            this(status, message, details, path, timestamp, null);
        }

        public ErrorResponse addAdditionalInfo(String key, String value) {
            Map<String, String> newErrors = new HashMap<>();
            if (errors != null) {
                newErrors.putAll(errors);
            }
            newErrors.put(key, value);
            return new ErrorResponse(status, message, details, path, timestamp, newErrors);
        }
    }

    public record ValidationErrorResponse(int status, String message, LocalDateTime timestamp, Map<String, String> errors) {}
}
