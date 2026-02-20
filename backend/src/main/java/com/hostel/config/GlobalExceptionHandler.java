package com.hostel.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.hostel.exception.GeminiApiException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler for all REST endpoints
 * Provides consistent error response format across the application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle Gemini API exceptions
     */
    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<ErrorResponse> handleGeminiApiException(
            GeminiApiException ex,
            HttpServletRequest request) {
        logger.error("Gemini API Exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle validation errors (e.g., @Valid on request body)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        logger.warn("Validation error on request: {}", request.getRequestURI());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        String message = "Validation failed: " + fieldErrors.values().stream()
            .findFirst().orElse("Invalid request");
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Validation Error",
            HttpStatus.BAD_REQUEST.value(),
            message,
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Bad Request",
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {
        logger.warn("Resource not found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Not Found",
            HttpStatus.NOT_FOUND.value(),
            "The requested resource was not found",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle method argument type mismatch (e.g., wrong path variable type)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeArgumentMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        logger.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());
        
        String message = String.format("Invalid value for parameter '%s': %s",
            ex.getName(),
            ex.getValue());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Bad Request",
            HttpStatus.BAD_REQUEST.value(),
            message,
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {
        logger.error("Runtime exception", ex);
        
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message,
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        logger.error("Unhandled exception", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Standard error response format
     */
    public static class ErrorResponse {
        private String error;
        private int status;
        private String message;
        private String path;
        private long timestamp;

        public ErrorResponse(String error, int status, String message, String path) {
            this.error = error;
            this.status = status;
            this.message = message;
            this.path = path;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters for JSON serialization
        public String getError() { return error; }
        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public long getTimestamp() { return timestamp; }
    }
}
