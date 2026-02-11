package com.client.defectticket.lambda.classifier;

import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.Severity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Mock classification service for local development and testing.
 * Simulates AI classification using keyword-based rules.
 * 
 * Active only in 'local' and 'test' profiles.
 */
@Slf4j
@Service
@Profile({"local", "test"})
public class MockClassificationService {

    private static final Set<String> CRITICAL_KEYWORDS = Set.of(
        "crash", "critical", "security", "breach", "down", "outage", "data loss"
    );
    
    private static final Set<String> HIGH_KEYWORDS = Set.of(
        "error", "fail", "broken", "cannot", "unable", "bug"
    );
    
    private static final Set<String> SECURITY_KEYWORDS = Set.of(
        "security", "vulnerability", "injection", "xss", "authentication", "authorization"
    );

    @Value("${llm.confidence-threshold}")
    private double confidenceThreshold;

    /**
     * Mock classify method that simulates AI classification.
     * Uses keyword analysis to determine severity and category.
     */
    public Classification classify(DefectTicket ticket) {
        log.info("🎭 MOCK: Classifying ticket {} (local mode)", ticket.getTicketId());

        String title = ticket.getTitle().toLowerCase();
        String description = ticket.getDescription().toLowerCase();
        String combined = title + " " + description;

        Severity severity;
        String category;
        String subcategory;
        int priority;
        double confidence;
        String reasoning;

        // Determine severity and category based on keywords
        if (containsAny(combined, CRITICAL_KEYWORDS)) {
            severity = Severity.CRITICAL;
            priority = 1;
            confidence = 0.95;
            reasoning = "Mock classifier detected critical keywords";
            
            if (containsAny(combined, SECURITY_KEYWORDS)) {
                category = "Security";
                subcategory = "Critical Security Vulnerability";
            } else {
                category = "Bug";
                subcategory = "Critical System Failure";
            }
            
        } else if (containsAny(combined, HIGH_KEYWORDS)) {
            severity = Severity.HIGH;
            priority = 2;
            confidence = 0.85;
            reasoning = "Mock classifier detected high severity keywords";
            
            if (containsAny(combined, SECURITY_KEYWORDS)) {
                category = "Security";
                subcategory = "Security Issue";
            } else {
                category = "Bug";
                subcategory = "Major Functionality Issue";
            }
            
        } else if (containsAny(combined, Set.of("ui", "alignment", "cosmetic", "style"))) {
            severity = Severity.LOW;
            priority = 4;
            confidence = 0.75;
            reasoning = "Mock classifier detected UI/cosmetic issue";
            category = "Enhancement";
            subcategory = "UI/UX Improvement";
            
        } else {
            severity = Severity.MEDIUM;
            priority = 3;
            confidence = 0.70;
            reasoning = "Mock classifier default classification";
            category = "Bug";
            subcategory = "General Issue";
        }

        Classification classification = Classification.builder()
                .category(category)
                .subcategory(subcategory)
                .severity(severity)
                .priority(priority)
                .confidenceScore(confidence)
                .reasoning(reasoning + " (MOCK MODE - No real AI)")
                .classificationSource("MOCK_LLM")
                .requiresHumanApproval(confidence < confidenceThreshold)
                .build();

        log.info("🎭 MOCK: Classified {} as {} (severity: {}, confidence: {}, approval needed: {})",
                ticket.getTicketId(), category, severity, confidence, 
                classification.isRequiresHumanApproval());

        return classification;
    }

    private boolean containsAny(String text, Set<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
}
