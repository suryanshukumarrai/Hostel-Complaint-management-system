package com.hostel.service;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagLlmClient {

    private static final Logger logger = LoggerFactory.getLogger(RagLlmClient.class);

    @Value("${rag.llm.api.url}")
    private String apiUrl;

    @Value("${rag.llm.api.key}")
    private String apiKey;

    @Value("${rag.llm.model}")
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
            throw new IllegalStateException("RAG LLM API URL not configured. Set rag.llm.api.url in application.properties.");
        }
        logger.info("RagLlmClient initialized successfully with API URL: {}", apiUrl);
    }

    public String generateAnswer(String systemPrompt, String question, String context) {
        if (apiUrl == null || apiUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            return "LLM configuration is missing. Please set rag.llm.api.url and LLM_API_KEY.";
        }

        // Gemini REST API: POST generateContent with API key as query param and JSON body
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        String combined = systemPrompt + "\n\nContext:\n" + context + "\n\nQuestion: " + question;

        Map<String, Object> part = new HashMap<>();
        part.put("text", combined);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        body.put("contents", List.of(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String urlWithKey = apiUrl + (apiUrl.contains("?") ? "&" : "?") + "key=" + apiKey;

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(urlWithKey, entity, Map.class);
            
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.error("LLM API returned non-200 status: {}", responseEntity.getStatusCode());
                throw new RuntimeException("LLM API error: HTTP " + responseEntity.getStatusCode());
            }
            
            Map<String, Object> response = responseEntity.getBody();
            if (response == null) {
                throw new RuntimeException("No response from LLM API.");
            }
            
            logger.info("Raw LLM response: {}", response);
            
            // Check for error in response
            if (response.containsKey("error")) {
                Object errorObj = response.get("error");
                String errorMsg = "LLM API returned error";
                if (errorObj instanceof Map<?, ?> errorMap) {
                    Object messageObj = errorMap.get("message");
                    if (messageObj != null) {
                        errorMsg = messageObj.toString();
                    }
                }
                logger.error("LLM API error: {}", errorMsg);
                throw new RuntimeException("LLM API error: " + errorMsg);
            }

            Object candidatesObj = response.get("candidates");
            if (candidatesObj instanceof List<?> candidates && !candidates.isEmpty()) {
                Object first = candidates.get(0);
                if (first instanceof Map<?,?> firstMap) {
                    Object contentObj = firstMap.get("content");
                    if (contentObj instanceof Map<?,?> contentMap) {
                        Object partsObj = contentMap.get("parts");
                        if (partsObj instanceof List<?> parts && !parts.isEmpty()) {
                            Object firstPart = parts.get(0);
                            if (firstPart instanceof Map<?,?> partMap) {
                                Object textObj = partMap.get("text");
                                if (textObj instanceof String text) {
                                    return text;
                                }
                            }
                        }
                    }
                }
            }
            throw new RuntimeException("Unexpected response format from LLM API.");
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error calling LLM API", ex);
            throw new RuntimeException("Error calling LLM API: " + ex.getMessage(), ex);
        }
    }
}
