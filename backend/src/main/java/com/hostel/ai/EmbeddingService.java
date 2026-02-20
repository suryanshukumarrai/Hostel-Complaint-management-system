package com.hostel.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    private final RestTemplate restTemplate;
    private final String geminiApiKey;
    private final String embeddingUrl;

    public EmbeddingService(RestTemplate restTemplate,
                            @Value("${gemini.api.key}") String geminiApiKey,
                            @Value("${gemini.embedding.url}") String embeddingUrl) {
        this.restTemplate = restTemplate;
        this.geminiApiKey = geminiApiKey;
        this.embeddingUrl = embeddingUrl;
    }

    /**
     * Generate embedding vector for text using Gemini API
     */
    @SuppressWarnings("unchecked")
    public List<Float> generateEmbedding(String text) {
        try {
            logger.info("Generating embedding for text of length: {}", text.length());

            if (text.trim().isEmpty()) {
                logger.error("Cannot generate embedding for empty text");
                throw new IllegalArgumentException("Text cannot be empty");
            }

            String url = embeddingUrl + "?key=" + geminiApiKey;

            // Create request payload
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            
            part.put("text", text);
            content.put("parts", List.of(part));
            
            requestBody.put("content", content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            logger.debug("Calling Gemini embedding API");
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("embedding")) {
                Map<String, Object> embedding = (Map<String, Object>) response.get("embedding");
                List<Double> values = (List<Double>) embedding.get("values");
                
                // Convert Double to Float
                List<Float> floatValues = new ArrayList<>();
                for (Double d : values) {
                    floatValues.add(d.floatValue());
                }
                
                logger.info("Embedding generated successfully with {} dimensions", floatValues.size());
                return floatValues;
            } else {
                logger.error("Invalid response from Gemini API");
                throw new RuntimeException("Failed to generate embedding - invalid response");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error generating embedding: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Generate embedding with fallback to simple hash-based vector
     * This allows the system to work even if Gemini API is unavailable
     */
    public List<Float> generateEmbeddingWithFallback(String text) {
        try {
            return generateEmbedding(text);
        } catch (Exception e) {
            logger.warn("Gemini embedding failed, using fallback embedding: {}", e.getMessage());
            return generateFallbackEmbedding(text);
        }
    }

    /**
     * Generate a simple fallback embedding based on text hash
     * This ensures the system can still function without Gemini
     */
    private List<Float> generateFallbackEmbedding(String text) {
        logger.info("Using fallback embedding method");
        List<Float> embedding = new ArrayList<>();
        
        // Create a deterministic 384-dimensional vector based on text
        // This is a simplified approach; in production, use a proper embedding method
        long hashCode = text.hashCode();
        
        for (int i = 0; i < 384; i++) {
            // Use hash and index to generate deterministic values
            float value = ((float) Math.sin(hashCode + i)) / 10.0f;
            embedding.add(value);
        }
        
        logger.info("Fallback embedding generated with {} dimensions", embedding.size());
        return embedding;
    }
}
