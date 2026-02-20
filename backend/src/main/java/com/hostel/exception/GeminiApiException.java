package com.hostel.exception;

public class GeminiApiException extends RuntimeException {

    private final int status;
    private final String error;

    public GeminiApiException(String message) {
        this(message, 500, "Gemini API Failure");
    }

    public GeminiApiException(String message, int status, String error) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
