package com.hostel.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GeminiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GeminiExceptionHandler.class);

    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<Object> handleGeminiApiException(GeminiApiException ex, WebRequest request) {
        logger.error("Gemini API exception: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", ex.getStatus());
        body.put("error", ex.getError());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        HttpStatus status = ex.getStatus() >= 400 && ex.getStatus() < 600
                ? HttpStatus.valueOf(ex.getStatus())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(body, status);
    }
}
