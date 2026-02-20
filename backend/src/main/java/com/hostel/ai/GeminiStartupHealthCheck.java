package com.hostel.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostel.config.GeminiConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "gemini.api.health-check.enabled", havingValue = "true")
public class GeminiStartupHealthCheck implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(GeminiStartupHealthCheck.class);

    private final RestTemplate restTemplate;
    private final GeminiConfigProperties geminiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiStartupHealthCheck(RestTemplate restTemplate, GeminiConfigProperties geminiConfig) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        String baseUrl = geminiConfig.buildGenerateContentUrl();
        String url = baseUrl + "?key=" + geminiConfig.getKey();

        logger.info("Running Gemini startup health check (dev profile)");
        logger.info("Health check URL: {}", maskUrl(url));

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", "Respond with OK.");
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Gemini health check failed with status: " + response.getStatusCode());
            }

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new IllegalStateException("Gemini health check returned empty response.");
            }

            objectMapper.readValue(body, Map.class);
            logger.info("Gemini health check passed.");
        } catch (HttpClientErrorException e) {
            throw new IllegalStateException("Gemini health check failed: " + e.getStatusCode() + " - " + safeBody(e.getResponseBodyAsString()), e);
        } catch (Exception e) {
            throw new IllegalStateException("Gemini health check failed: " + e.getMessage(), e);
        }
    }

    private String maskUrl(String url) {
        int idx = url.indexOf("?key=");
        if (idx == -1) {
            return url;
        }

        return url.substring(0, idx + 5) + "****";
    }

    private String safeBody(String body) {
        if (body == null) {
            return "<empty>";
        }

        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }
}
