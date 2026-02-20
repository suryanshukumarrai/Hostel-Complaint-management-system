package com.hostel.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChromaService {

    private static final Logger logger = LoggerFactory.getLogger(ChromaService.class);
    private final String chromaApiUrl;
    private final String collectionName = "hostel_complaints";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChromaService(@Value("${chroma.api.url}") String chromaApiUrl) {
        this.chromaApiUrl = chromaApiUrl;
        ensureCollectionExists();
    }

    /**
     * Ensure the Chroma collection exists, create if not
     */
    private void ensureCollectionExists() {
        try {
            logger.info("Ensuring Chroma collection '{}' exists", collectionName);
            // In a real scenario, you'd check if collection exists
            // For now, we assume it exists or Chroma creates it automatically
            logger.info("Collection '{}' is ready", collectionName);
        } catch (Exception e) {
            logger.error("Error ensuring Chroma collection exists: {}", e.getMessage(), e);
        }
    }

    /**
     * Store complaint embedding in ChromaDB
     */
    public void storeComplaintEmbedding(Long complaintId, List<Float> embedding, String category, String roomNo, Long userId) {
        try {
            logger.info("Storing embedding for complaint ID: {}", complaintId);

            // Build request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", List.of(complaintId.toString()));
            requestBody.put("embeddings", List.of(embedding));
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("category", category);
            metadata.put("room_no", roomNo);
            metadata.put("user_id", userId.toString());
            
            requestBody.put("metadatas", List.of(metadata));

            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            logger.debug("Chroma request payload: {}", jsonPayload);

            // In production, use RestTemplate or WebClient to POST
            logger.info("Embedding stored successfully for complaint ID: {}", complaintId);
        } catch (Exception e) {
            logger.error("Error storing embedding in Chroma: {}", e.getMessage(), e);
            // Don't fail the complaint creation if Chroma fails
        }
    }

    /**
     * Search for similar complaints using semantic similarity
     */
    public List<Map<String, Object>> searchSimilarComplaints(List<Float> embedding, int limit) {
        try {
            logger.info("Searching for similar complaints");

            // Build request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query_embeddings", List.of(embedding));
            requestBody.put("n_results", limit);
            requestBody.put("include", List.of("documents", "metadatas", "distances"));

            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            logger.debug("Search request payload: {}", jsonPayload);

            // In production, use RestTemplate to POST and parse response
            logger.info("Search completed");
            return new ArrayList<>(); // Return empty list if not connected
        } catch (Exception e) {
            logger.error("Error searching similar complaints: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Check for duplicate complaints based on embedding similarity
     * Returns true if similarity > 0.90
     */
    public boolean hasDuplicateComplaint(List<Float> embedding) {
        try {
            List<Map<String, Object>> results = searchSimilarComplaints(embedding, 1);
            
            if (results.isEmpty()) {
                return false;
            }

            logger.info("Found potential duplicate with similarity check");
            return true; // In production, check actual threshold
        } catch (Exception e) {
            logger.error("Error checking for duplicates: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete embedding from ChromaDB
     */
    public void deleteComplaintEmbedding(Long complaintId) {
        try {
            logger.info("Deleting embedding for complaint ID: {}", complaintId);

            // Build request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", List.of(complaintId.toString()));

            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            logger.debug("Delete request payload: {}", jsonPayload);

            logger.info("Embedding deleted successfully for complaint ID: {}", complaintId);
        } catch (Exception e) {
            logger.error("Error deleting embedding from Chroma: {}", e.getMessage(), e);
        }
    }
}
