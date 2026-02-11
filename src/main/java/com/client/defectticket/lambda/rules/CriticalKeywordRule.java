package com.client.defectticket.lambda.rules;

import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.Severity;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Rule that detects critical keywords and escalates severity.
 * Highest priority rule that overrides AI classification.
 */
@Component
public class CriticalKeywordRule implements ClassificationRule {

    private static final Set<String> CRITICAL_KEYWORDS = Set.of(
            "security breach", 
            "data loss", 
            "system down", 
            "production outage",
            "critical error", 
            "cannot login", 
            "payment failed",
            "sql injection",
            "xss vulnerability",
            "authentication bypass",
            "ddos attack"
    );

    @Override
    public boolean applies(DefectTicket ticket) {
        String text = (ticket.getTitle() + " " + ticket.getDescription()).toLowerCase();
        return CRITICAL_KEYWORDS.stream().anyMatch(text::contains);
    }

    @Override
    public Classification evaluate(DefectTicket ticket) {
        return Classification.builder()
                .category("Critical Issue")
                .subcategory("High Priority Escalation")
                .severity(Severity.CRITICAL)
                .priority(1)
                .confidenceScore(1.0)
                .reasoning("Triggered by critical keyword detection - immediate attention required")
                .classificationSource("RULES")
                .requiresHumanApproval(true)  // Always require human review for critical issues
                .build();
    }

    @Override
    public int priority() {
        return 1;  // Highest priority
    }
}
