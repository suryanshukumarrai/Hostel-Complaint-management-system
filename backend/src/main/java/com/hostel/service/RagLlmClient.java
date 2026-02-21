package com.hostel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagLlmClient {

    @Value("${rag.llm.api.url}")
    private String apiUrl;

    @Value("${rag.llm.api.key}")
    private String apiKey;

    @Value("${rag.llm.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

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
            Map<String, Object> response = restTemplate.postForObject(urlWithKey, entity, Map.class);
            if (response == null) {
                return "No response from LLM API.";
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
            return "Unexpected response format from LLM API.";
        } catch (Exception ex) {
            return "Error calling LLM API: " + ex.getMessage();
        }
    }
}
