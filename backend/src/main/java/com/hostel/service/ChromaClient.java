package com.hostel.service;

import com.hostel.entity.Category;
import com.hostel.entity.Complaint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ChromaClient {

    @Value("${chroma.url:}")
    private String chromaUrl;

    @Value("${chroma.collection:hostel_complaints_embeddings}")
    private String collection;

    @Autowired
    private GeminiEmbeddingClient embeddingClient;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(?:\\+?\\d[\\s-]?){7,15}(?!\\d)");

    public void ensureCollection() {
        if (chromaUrl == null || chromaUrl.isBlank()) {
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("name", collection);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForLocation(chromaUrl + "/api/v1/collections", entity);
        } catch (Exception ex) {
            // Ignore if collection already exists or server is not reachable
        }
    }

    public void upsertComplaint(Complaint complaint) {
        if (complaint == null || complaint.getId() == null) {
            return;
        }
        if (chromaUrl == null || chromaUrl.isBlank()) {
            return;
        }
        if (complaint.getCategory() == null || complaint.getDescription() == null || complaint.getDescription().isBlank()) {
            return;
        }

        String masked = maskPii(complaint.getDescription());
        List<Double> embedding = embeddingClient.embed(masked);
        if (embedding.isEmpty()) {
            return;
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("category", complaint.getCategory().name());

        Map<String, Object> body = new HashMap<>();
        body.put("ids", List.of(String.valueOf(complaint.getId())));
        body.put("embeddings", List.of(embedding));
        body.put("metadatas", List.of(meta));
        body.put("documents", List.of(masked));

        postJson(chromaUrl + "/api/v1/collections/" + collection + "/upsert", body);
    }

    public List<QueryCandidate> queryCategories(String description, int topK) {
        if (description == null || description.isBlank()) {
            return Collections.emptyList();
        }
        if (chromaUrl == null || chromaUrl.isBlank()) {
            return Collections.emptyList();
        }

        String masked = maskPii(description);
        List<Double> embedding = embeddingClient.embed(masked);
        if (embedding.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> body = new HashMap<>();
        body.put("query_embeddings", List.of(embedding));
        body.put("n_results", Math.max(1, topK));
        body.put("include", List.of("metadatas", "distances"));

        Map<String, Object> response = postJson(chromaUrl + "/api/v1/collections/" + collection + "/query", body);
        if (response == null) {
            return Collections.emptyList();
        }

        List<QueryCandidate> candidates = new ArrayList<>();
        List<?> metadatasNested = getFirstNestedList(response.get("metadatas"));
        List<?> distancesNested = getFirstNestedList(response.get("distances"));

        for (int i = 0; i < metadatasNested.size(); i++) {
            Object metaObj = metadatasNested.get(i);
            String category = null;
            if (metaObj instanceof Map<?, ?> metaMap) {
                Object catObj = metaMap.get("category");
                if (catObj != null) {
                    category = String.valueOf(catObj).toUpperCase(Locale.ROOT);
                }
            }
            Double distance = null;
            if (i < distancesNested.size()) {
                Object distObj = distancesNested.get(i);
                if (distObj instanceof Number number) {
                    distance = number.doubleValue();
                }
            }
            if (category != null) {
                candidates.add(new QueryCandidate(category, distance));
            }
        }

        return candidates;
    }

    private Map<String, Object> postJson(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            return response;
        } catch (Exception ex) {
            return null;
        }
    }

    private List<?> getFirstNestedList(Object nested) {
        if (nested instanceof List<?> outer && !outer.isEmpty()) {
            Object first = outer.get(0);
            if (first instanceof List<?> inner) {
                return inner;
            }
        }
        return Collections.emptyList();
    }

    private String maskPii(String text) {
        String masked = EMAIL_PATTERN.matcher(text).replaceAll("[email hidden]");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("[phone hidden]");
        return masked;
    }

    public record QueryCandidate(String category, Double distance) {
        public Category asCategory() {
            try {
                return Category.valueOf(category);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
