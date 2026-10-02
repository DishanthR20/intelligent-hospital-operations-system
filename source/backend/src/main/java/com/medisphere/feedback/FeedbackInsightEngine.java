package com.medisphere.feedback;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Feedback Insight Engine (spec section 20): a rule-based keyword theme
 * analyzer over free-text feedback comments. This is explicitly NOT natural
 * language understanding or sentiment AI — just configurable keyword-to-theme
 * matching, counted and reported as percentages of total mentions.
 */
@Component
public class FeedbackInsightEngine {

    private static final Map<String, List<String>> THEME_KEYWORDS = Map.of(
            "Waiting Time", List.of("wait", "waiting", "queue", "slow", "delay"),
            "Billing", List.of("bill", "billing", "payment", "charge", "invoice", "expensive"),
            "Doctor", List.of("doctor", "dr.", "physician", "consultation"),
            "Staff", List.of("staff", "nurse", "receptionist", "rude", "unfriendly", "helpful"),
            "Cleanliness", List.of("clean", "dirty", "hygiene", "sanitary"),
            "Pharmacy", List.of("pharmacy", "medicine", "medication", "drug"),
            "Laboratory", List.of("lab ", "laboratory", "test", "sample", "report"),
            "Appointments", List.of("appointment", "schedule", "booking", "reschedule"),
            "Communication", List.of("communication", "explain", "informed", "told", "unclear")
    );

    public List<FeedbackTheme> analyze(List<PatientFeedback> feedbackList) {
        Map<String, Long> mentionCounts = new LinkedHashMap<>();
        THEME_KEYWORDS.keySet().forEach(theme -> mentionCounts.put(theme, 0L));
        long otherCount = 0;

        for (PatientFeedback feedback : feedbackList) {
            String text = feedback.getComments();
            if (text == null || text.isBlank()) continue;
            String lower = text.toLowerCase();

            boolean matchedAny = false;
            for (var entry : THEME_KEYWORDS.entrySet()) {
                boolean matches = entry.getValue().stream().anyMatch(lower::contains);
                if (matches) {
                    mentionCounts.merge(entry.getKey(), 1L, Long::sum);
                    matchedAny = true;
                }
            }
            if (!matchedAny) otherCount++;
        }

        long total = mentionCounts.values().stream().mapToLong(Long::longValue).sum() + otherCount;
        if (total == 0) return List.of();

        var results = new java.util.ArrayList<FeedbackTheme>();
        mentionCounts.forEach((theme, count) -> {
            if (count > 0) results.add(new FeedbackTheme(theme, count, round(count * 100.0 / total)));
        });
        if (otherCount > 0) results.add(new FeedbackTheme("Other", otherCount, round(otherCount * 100.0 / total)));

        return results.stream().sorted((a, b) -> Long.compare(b.mentions(), a.mentions())).toList();
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
