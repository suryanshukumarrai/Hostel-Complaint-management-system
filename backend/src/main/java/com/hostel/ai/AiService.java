package com.hostel.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostel.config.GeminiConfigProperties;
import com.hostel.dto.AiComplaintResponse;
import com.hostel.dto.StructuredComplaintData;
import com.hostel.entity.Complaint;
import com.hostel.entity.User;
import com.hostel.exception.GeminiApiException;
import com.hostel.repository.ComplaintRepository;
import com.hostel.repository.UserRepository;
import com.hostel.service.ComplaintMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);
    
    private final RestTemplate restTemplate;
    private final EmbeddingService embeddingService;
    private final ChromaService chromaService;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ComplaintMappingService complaintMappingService;
    private final GeminiConfigProperties geminiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PROMPT_TEMPLATE =
        "Return ONLY valid JSON matching this exact schema (no markdown, no explanation, no extra keys):\n" +
        "{\n" +
        "  \"category\": \"PLUMBING | ELECTRICAL | RAGGING | CARPENTRY\",\n" +
        "  \"subCategory\": \"string\",\n" +
        "  \"roomNo\": \"string\",\n" +
        "  \"block\": \"string\",\n" +
        "  \"roomType\": \"Single | Double\",\n" +
        "  \"priorityLevel\": 1,\n" +
        "  \"assignedTeam\": \"string\",\n" +
        "  \"preferredTimeSlot\": \"string\"\n" +
        "}\n\n" +
        "Description:\n%s";

    public AiService(RestTemplate restTemplate,
                     EmbeddingService embeddingService,
                     ChromaService chromaService,
                     ComplaintRepository complaintRepository,
                     UserRepository userRepository,
                     ComplaintMappingService complaintMappingService,
                     GeminiConfigProperties geminiConfig) {
        this.restTemplate = restTemplate;
        this.embeddingService = embeddingService;
        this.chromaService = chromaService;
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.complaintMappingService = complaintMappingService;
        this.geminiConfig = geminiConfig;

        logger.info("Ensure Generative Language API is enabled for the project linked to this key.");
    }

    /**
     * Main method to generate complaint from description using AI
     */
    public AiComplaintResponse generateComplaintFromDescription(String description, Long userId) {
        try {
            logger.info("Starting AI complaint generation for user ID: {}", userId);

            // Validate input
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be empty");
            }

            // Get user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Step 1: Call LLM to get structured data
            logger.info("Step 1: Calling Gemini API to structure complaint");
            StructuredComplaintData structuredData = callGeminiApi(description);
            logger.info("Step 1 completed: Got structured data with category: {}", structuredData.getCategory());

            // Step 2: Generate embedding
            logger.info("Step 2: Generating embedding for description");
            List<Float> embedding = embeddingService.generateEmbeddingWithFallback(description);
            logger.info("Step 2 completed: Embedding generated with {} dimensions", embedding.size());

            // Step 3: Check for duplicates (bonus feature)
            logger.info("Step 3: Checking for duplicate complaints");
            boolean isDuplicate = chromaService.hasDuplicateComplaint(embedding);
            if (isDuplicate) {
                logger.warn("Potential duplicate complaint detected");
            }
            logger.info("Step 3 completed: Duplicate check done");

            // Step 4: Create and save complaint
            logger.info("Step 4: Creating complaint entity");
            Complaint complaint = complaintMappingService.mapFromAi(structuredData, description, user);
            Complaint savedComplaint = complaintRepository.save(complaint);
            logger.info("Step 4 completed: Complaint saved with ID: {}", savedComplaint.getId());

            // Step 5: Store embedding in ChromaDB
            logger.info("Step 5: Storing embedding in ChromaDB");
            chromaService.storeComplaintEmbedding(
                    savedComplaint.getId(),
                    embedding,
                    structuredData.getCategory(),
                    structuredData.getRoomNo(),
                    userId
            );
            logger.info("Step 5 completed: Embedding stored in ChromaDB");

            // Step 6: Return response
            logger.info("AI complaint generation completed successfully");
            return buildResponse(savedComplaint, structuredData, isDuplicate);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (GeminiApiException e) {
            logger.error("Gemini API error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error generating complaint: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating complaint", e);
        }
    }

    /**
     * Call Gemini API to structure complaint
     */
    @SuppressWarnings("unchecked")
    private StructuredComplaintData callGeminiApi(String description) {
        try {
            String prompt = String.format(PROMPT_TEMPLATE, description);
            logger.debug("Sending prompt to Gemini API (length: {})", prompt.length());

            String key = geminiConfig.getKey();
            String baseUrl = geminiConfig.buildGenerateContentUrl();
            String url = buildUrlWithKey(baseUrl, key);
            logApiDiagnostics(key, url);

            // Build request
            Map<String, Object> requestBody = buildGeminiRequest(prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Call API
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.error("Gemini API call failed with status: {}", responseEntity.getStatusCode());
                throw new GeminiApiException("Gemini API Failure", 500, "Gemini API Failure");
            }

            String responseBody = responseEntity.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                logger.error("Empty response body from Gemini API");
                throw new GeminiApiException("Gemini API Failure", 500, "Gemini API Failure");
            }

            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            // Parse response
            StructuredComplaintData result = parseGeminiResponse(response);
            logger.info("Successfully parsed structured data from Gemini");
            return result;

        } catch (HttpClientErrorException e) {
            handleClientError(e);
            throw e;
        } catch (HttpServerErrorException e) {
            logger.error("Gemini API server error ({}): {}", e.getStatusCode(), safeBody(e.getResponseBodyAsString()));
            throw new GeminiApiException("Gemini API Failure", 500, "Gemini API Failure");
        } catch (GeminiApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error calling Gemini API: {}", e.getMessage(), e);
            throw new GeminiApiException("Gemini API Failure", 500, "Gemini API Failure");
        }
    }

    private void logApiDiagnostics(String key, String url) {
        boolean keyLoaded = key != null && !key.isBlank();
        logger.info("Gemini API key loaded: {}", keyLoaded);
        if (keyLoaded) {
            logger.info("Gemini API key length: {}", key.length());
        }

        logger.info("Gemini API URL (with key): {}", maskUrl(url));
    }

    private String buildUrlWithKey(String baseUrl, String key) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Gemini API URL is blank.");
        }

        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Gemini API key is missing.");
        }

        if (baseUrl.contains("?key=")) {
            throw new IllegalStateException("Gemini API base URL must not contain ?key=.");
        }

        String url = baseUrl + "?key=" + key;
        if (!url.contains("?key=")) {
            throw new IllegalStateException("Gemini API URL missing ?key= parameter.");
        }

        if (url.contains("\n") || url.contains("\r") || url.contains(" ")) {
            throw new IllegalStateException("Gemini API URL contains whitespace or newline characters.");
        }

        return url;
    }

    private void handleClientError(HttpClientErrorException e) {
        String body = e.getResponseBodyAsString();
        String safeBody = safeBody(body);
        logger.error("Gemini API client error ({}): {}", e.getStatusCode(), safeBody);

        int status = e.getStatusCode().value();
        if (status == 404) {
            throw new GeminiApiException("Model endpoint not found or API misconfigured", 500, "Gemini API Failure");
        }

        if (status == 400) {
            String reason = extractErrorReason(body);
            if ("API_KEY_INVALID".equalsIgnoreCase(reason)) {
                throw new GeminiApiException("Gemini API key invalid or restricted. Check GCP console.", 500, "Gemini API Failure");
            }
            throw new GeminiApiException("Gemini API request rejected. Check API key and payload.", 500, "Gemini API Failure");
        }

        if (status == 401 || status == 403) {
            throw new GeminiApiException("Gemini API key invalid or restricted. Check GCP console.", 500, "Gemini API Failure");
        }
    }

    private String extractErrorReason(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        try {
            Map<String, Object> response = objectMapper.readValue(body, Map.class);
            Object errorObj = response.get("error");
            if (!(errorObj instanceof Map)) {
                return null;
            }

            Map<String, Object> errorMap = (Map<String, Object>) errorObj;
            Object detailsObj = errorMap.get("details");
            if (!(detailsObj instanceof List)) {
                return null;
            }

            List<?> details = (List<?>) detailsObj;
            for (Object detail : details) {
                if (detail instanceof Map) {
                    Object reason = ((Map<?, ?>) detail).get("reason");
                    if (reason instanceof String) {
                        return (String) reason;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to parse Gemini error reason: {}", e.getMessage());
        }

        if (body.contains("API_KEY_INVALID")) {
            return "API_KEY_INVALID";
        }

        return null;
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

    /**
     * Build Gemini API request body
     */
    private Map<String, Object> buildGeminiRequest(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", List.of(part));
        
        requestBody.put("contents", List.of(content));

        return requestBody;
    }

    /**
     * Parse Gemini API response and extract structured data
     */
    @SuppressWarnings("unchecked")
    private StructuredComplaintData parseGeminiResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            
            if (candidates == null || candidates.isEmpty()) {
                logger.error("No candidates in Gemini response");
                throw new RuntimeException("No response from Gemini");
            }

            Map<String, Object> candidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) candidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                logger.error("No parts in Gemini response");
                throw new RuntimeException("Invalid Gemini response");
            }

            String jsonString = (String) parts.get(0).get("text");
            logger.debug("Raw response from Gemini: {}", jsonString);

            // Extract JSON from response (may contain markdown code blocks)
            String jsonContent = extractJsonFromResponse(jsonString);
            
            // Parse JSON
            StructuredComplaintData data = objectMapper.readValue(jsonContent, StructuredComplaintData.class);
            logger.info("Parsed structured data: category={}, room={}, priority={}", 
                    data.getCategory(), data.getRoomNo(), data.getPriorityLevel());
            
            return data;

        } catch (RuntimeException e) {
            logger.error("Error parsing Gemini response: {}", e.getMessage(), e);
            throw new RuntimeException("Error parsing Gemini response: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error parsing Gemini response: {}", e.getMessage(), e);
            throw new RuntimeException("Error parsing Gemini response: " + e.getMessage(), e);
        }
    }

    /**
     * Extract JSON from response that may contain markdown code blocks
     */
    private String extractJsonFromResponse(String response) {
        // Try to find JSON in code blocks first
        Pattern jsonPattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");
        Matcher matcher = jsonPattern.matcher(response);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // If no code block, treat entire response as JSON
        return response.trim();
    }


    /**
     * Build response DTO
     */
    private AiComplaintResponse buildResponse(Complaint savedComplaint, StructuredComplaintData structuredData, boolean isDuplicate) {
        AiComplaintResponse response = new AiComplaintResponse(
                savedComplaint.getId(),
                structuredData.getCategory(),
                structuredData.getSubCategory(),
                structuredData.getRoomNo(),
            structuredData.getPriorityLevel(),
                savedComplaint.getStatus().toString(),
                savedComplaint.getDescription()
        );

        if (isDuplicate) {
            response.setMessage("Complaint generated successfully (similar complaint exists)");
        }

        return response;
    }
}
