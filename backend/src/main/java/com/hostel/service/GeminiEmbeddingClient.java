package com.hostel.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

@Service
public class GeminiEmbeddingClient {

    private static final Logger logger = LoggerFactory.getLogger(GeminiEmbeddingClient.class);

    @Value("${gemini.embed.api.url}")
    private String apiUrl;

    @Value("${rag.llm.api.key}")
    private String apiKey;

    @Value("${gemini.embed.model:embedding-001}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Gemini API key not configured. Set GEMINI_API_KEY environment variable before running the application. " +
                "Get your API key from: https://aistudio.google.com/apikey"
            );
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("Gemini Embedding API URL not configured. Set gemini.embed.api.url in application.properties.");
        }
        logger.info("GeminiEmbeddingClient initialized successfully with model: {}", model);
    }

    public List<Double> embed(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        if (apiUrl == null || apiUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            return Collections.emptyList();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> part = new HashMap<>();
        part.put("text", text);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("content", content);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String urlWithKey = apiUrl + (apiUrl.contains("?") ? "&" : "?") + "key=" + apiKey;

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(urlWithKey, entity, Map.class);
            
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.error("Gemini Embedding API returned non-200 status: {}", responseEntity.getStatusCode());
                return Collections.emptyList();
            }
            
            Map<String, Object> response = responseEntity.getBody();
            if (response == null) {
                logger.warn("No response from Gemini Embedding API");
                return Collections.emptyList();
            }
            
            // Check for error in response
            if (response.containsKey("error")) {
                Object errorObj = response.get("error");
                if (errorObj instanceof Map<?, ?> errorMap) {
                    Object messageObj = errorMap.get("message");
                    logger.error("Gemini Embedding API error: {}", messageObj != null ? messageObj : errorObj);
                }
                return Collections.emptyList();
            }
            
            Object embeddingObj = response.get("embedding");
            if (embeddingObj instanceof Map<?, ?> embeddingMap) {
                Object valuesObj = embeddingMap.get("values");
                if (valuesObj instanceof List<?> values) {
                    return values.stream()
                            .filter(v -> v instanceof Number)
                            .map(v -> ((Number) v).doubleValue())
                            .toList();
                }
            }
        } catch (Exception ex) {
            logger.error("Error calling Gemini Embedding API", ex);
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }
}
