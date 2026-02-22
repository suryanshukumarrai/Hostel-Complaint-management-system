package com.hostel.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        logger.error("Runtime exception occurred", ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        return ResponseEntity.internalServerError()
                .body("An unexpected error occurred. Please contact support if the problem persists.");
    }
}
