package com.hostel.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "gemini.api")
public class GeminiConfigProperties {

    private static final Logger logger = LoggerFactory.getLogger(GeminiConfigProperties.class);

    private String key;
    private String url;

    @PostConstruct
    public void validate() {
        String keyValue = key != null ? key.trim() : null;
        String apiUrl = url != null ? url.trim() : null;

        boolean keyLoaded = keyValue != null && !keyValue.isBlank();
        logger.info("Gemini API key loaded: {}", keyLoaded);
        if (keyLoaded) {
            logger.info("Gemini API key length: {}", keyValue.length());
        }

        if (!keyLoaded) {
            throw new IllegalStateException("Gemini API key is missing. Set GEMINI_API_KEY environment variable.");
        }

        if (keyValue.contains("\n") || keyValue.contains("\r") || keyValue.contains(" ")) {
            throw new IllegalStateException("Gemini API key contains whitespace or newline characters.");
        }

        if (keyValue.startsWith("\"") || keyValue.endsWith("\"")) {
            throw new IllegalStateException("Gemini API key contains quotes. Remove quotes from the key.");
        }

        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("Gemini API URL is missing.");
        }

        if (apiUrl.contains("?key=")) {
            throw new IllegalStateException("Gemini API URL must not contain the ?key= parameter.");
        }

        if (!apiUrl.contains("/models/") || !apiUrl.contains(":generateContent")) {
            throw new IllegalStateException("Gemini API URL must include /models/{model}:generateContent.");
        }

        if (apiUrl.contains("\n") || apiUrl.contains("\r") || apiUrl.contains(" ")) {
            throw new IllegalStateException("Gemini API URL contains whitespace or newline characters.");
        }

        logger.info("Ensure Generative Language API is enabled for the project linked to this key.");

        this.key = keyValue;
        this.url = apiUrl;
    }

    public String buildGenerateContentUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
