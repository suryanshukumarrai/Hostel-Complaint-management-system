package com.hostel.service;

import com.hostel.entity.Complaint;
import com.hostel.entity.User;
import com.hostel.entity.QaHistory;
import com.hostel.repository.ComplaintRepository;
import com.hostel.repository.QaHistoryRepository;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
public class AgentQaService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RagLlmClient ragLlmClient;

    @Autowired
    private QaHistoryRepository qaHistoryRepository;

    /**
     * Simple retrieval-style answer generator over complaints assigned to the agent.
     * This is the "R" in RAG; you can plug an external LLM on top of the retrieved
     * snippets to generate richer natural language answers.
     */
    public String answerQuestion(String question, Long userId) {
        User client = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + userId));

        // Retrieve complaints raised by this client
        List<Complaint> allComplaints = complaintRepository.findByRaisedBy(client);

        if (allComplaints.isEmpty()) {
            return "No complaints found for client " + client.getFullName() + ".";
        }

        // Filter complaints by keywords in the question
        List<Complaint> filteredComplaints = filterComplaintsByQuestion(allComplaints, question);

        // Build retrieval context from the filtered complaints (limit to 20)
        String context = filteredComplaints.stream().limit(20).map(c -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Complaint #").append(c.getId()).append("\n");
            sb.append("Category: ").append(c.getCategory()).append("\n");
            sb.append("Status: ").append(c.getStatus()).append("\n");
            if (c.getAvailabilityDate() != null) {
                sb.append("Date: ").append(c.getAvailabilityDate()).append("\n");
            }
            if (c.getDescription() != null) {
                sb.append("Description: ").append(maskPii(c.getDescription())).append("\n");
            }
            sb.append("---\n");
            return sb.toString();
        }).collect(Collectors.joining("\n"));

        String systemPrompt = "You are a helpful hostel complaint management assistant. " +
            "Answer questions about the client's complaints using ONLY the provided context. " +
            "If the answer is not in the context, say you don't know. " +
            "Always format your answer EXACTLY in three sections with these headings: " +
            "'Summary', 'Details', and 'Suggestions'. " +
            "When the user asks 'how many', 'count', 'number of' or to 'show/list all' complaints " +
            "for a category (for example plumbing / plumbering), in the Summary give the exact count, and in Details: " +
            "list EACH matching complaint on its own line in this format: " +
            "'- Complaint #<id> | Category: <category> | Status: <status> | Date: <date or N/A> | <short description>'. " +
            "For other questions, still use the same three sections: " +
            "Summary: 1-2 sentence overview; Details: bullet points with ids, categories, statuses, dates; " +
            "Suggestions: bullet points with practical advice for the client (or 'None' if not applicable).";

        String answer = ragLlmClient.generateAnswer(systemPrompt, question, context);

        // persist history for this client question
        saveHistory(userId, false, question, answer);

        return answer;
    }

    /**
     * Admin-level Q&A over all complaints in the system.
     */
    public String answerAdminQuestion(String question, Long adminUserId) {
        List<Complaint> allComplaints = complaintRepository.findAll();

        if (allComplaints.isEmpty()) {
            return "No complaints found in the system.";
        }

        // Filter complaints by keywords in the question
        List<Complaint> filteredComplaints = filterComplaintsByQuestion(allComplaints, question);

        String context = filteredComplaints.stream().limit(50).map(c -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Complaint #").append(c.getId()).append("\n");
            // Do not include any PII such as full names or emails in the AI context
            sb.append("Raised By: ");
            if (c.getRaisedBy() != null && c.getRaisedBy().getId() != null) {
                sb.append("USER-").append(c.getRaisedBy().getId());
            } else {
                sb.append("Unknown");
            }
            sb.append("\n");
            sb.append("Category: ").append(c.getCategory()).append("\n");
            sb.append("Status: ").append(c.getStatus()).append("\n");
            if (c.getAvailabilityDate() != null) {
                sb.append("Date: ").append(c.getAvailabilityDate()).append("\n");
            }
            if (c.getDescription() != null) {
                sb.append("Description: ").append(maskPii(c.getDescription())).append("\n");
            }
            sb.append("---\n");
            return sb.toString();
        }).collect(Collectors.joining("\n"));

        String systemPrompt = "You are a helpful hostel complaint management assistant for admins. " +
            "Answer questions about all complaints in the system using ONLY the provided context. " +
            "If the answer is not in the context, say you don't know. " +
            "Always format your answer EXACTLY in three sections with these headings: " +
            "'Summary', 'Details', and 'Recommendations'. " +
            "When the admin asks 'how many', 'count', 'number of' or to 'show/list all' complaints " +
            "for a category (for example plumbing / plumbering), in the Summary give the exact count, and in Details: " +
            "list EACH matching complaint on its own line in this format: " +
            "'- Complaint #<id> | Raised By: <non-PII user id like USER-123 or Unknown> | Category: <category> | Status: <status> | Date: <date or N/A> | <short description>'. " +
            "For other questions, still use the same three sections: " +
            "Summary: brief overview; Details: bullet points with important numbers, categories, trends, and risks; " +
            "Recommendations: 1-3 concrete next actions for the admin.";

        String answer = ragLlmClient.generateAnswer(systemPrompt, question, context);

        // persist history for this admin question
        if (adminUserId != null) {
            saveHistory(adminUserId, true, question, answer);
        }

        return answer;
    }

    // Simple keyword-based filtering for complaints based on the question
    private List<Complaint> filterComplaintsByQuestion(List<Complaint> complaints, String question) {
        if (question == null || question.isEmpty()) return complaints;
        String q = question.toLowerCase(Locale.ROOT);

        // Category keywords
        boolean wantPlumbing = q.contains("plumbing") || q.contains("plumber");
        boolean wantElectrical = q.contains("electrical") || q.contains("electrician");
        boolean wantCarpentry = q.contains("carpentry") || q.contains("carpenter");
        boolean wantRagging = q.contains("ragging");

        // Status keywords
        boolean wantOpen = q.contains("open");
        boolean wantResolved = q.contains("resolved") || q.contains("closed") || q.contains("solved");
        boolean wantInProgress = q.contains("in progress") || q.contains("progress");

        // Filter by category and status if mentioned
        return complaints.stream().filter(c -> {
            boolean catMatch =
                (!wantPlumbing && !wantElectrical && !wantCarpentry && !wantRagging) ||
                (wantPlumbing && c.getCategory() != null && c.getCategory().toString().toLowerCase(Locale.ROOT).contains("plumb")) ||
                (wantElectrical && c.getCategory() != null && c.getCategory().toString().toLowerCase(Locale.ROOT).contains("electric")) ||
                (wantCarpentry && c.getCategory() != null && c.getCategory().toString().toLowerCase(Locale.ROOT).contains("carpent")) ||
                (wantRagging && c.getCategory() != null && c.getCategory().toString().toLowerCase(Locale.ROOT).contains("ragging"));

            boolean statusMatch =
                (!wantOpen && !wantResolved && !wantInProgress) ||
                (wantOpen && c.getStatus() != null && c.getStatus().toString().toLowerCase(Locale.ROOT).contains("open")) ||
                (wantResolved && c.getStatus() != null && c.getStatus().toString().toLowerCase(Locale.ROOT).contains("resolved")) ||
                (wantInProgress && c.getStatus() != null && c.getStatus().toString().toLowerCase(Locale.ROOT).contains("progress"));

            return catMatch && statusMatch;
        }).collect(Collectors.toList());
    }

    // Basic regex-based masking of PII in free-text fields before sending to the LLM
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}");

    // Very loose phone pattern: sequences of 7-15 digits (optionally with spaces, dashes, or +country code)
    // This is intentionally broad to avoid leaking phone numbers; it may sometimes over-mask.
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(?:\\+?\\d[\\s-]?){7,15}(?!\\d)");

    private String maskPii(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String masked = EMAIL_PATTERN.matcher(text).replaceAll("[email hidden]");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("[phone hidden]");
        return masked;
    }

    private void saveHistory(Long userId, boolean admin, String question, String answer) {
        if (userId == null || question == null || answer == null) {
            return;
        }
        QaHistory history = new QaHistory(userId, admin, question, answer, LocalDateTime.now());
        qaHistoryRepository.save(history);
    }
}
