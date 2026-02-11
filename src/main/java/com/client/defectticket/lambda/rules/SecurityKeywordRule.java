package com.client.defectticket.lambda.rules;

import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.Severity;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Rule that detects security-related issues.
 * Priority 2 - runs after critical keywords but before general rules.
 */
@Component
public class SecurityKeywordRule implements ClassificationRule {

    private static final Set<String> SECURITY_KEYWORDS = Set.of(
            "security",
            "vulnerability",
            "exploit",
            "injection",
            "authentication",
            "authorization",
            "encryption",
            "credential",
            "password",
            "token leak",
            "csrf",
            "sensitive data"
    );

    @Override
    public boolean applies(DefectTicket ticket) {
        String text = (ticket.getTitle() + " " + ticket.getDescription()).toLowerCase();
        return SECURITY_KEYWORDS.stream().anyMatch(text::contains);
    }

    @Override
    public Classification evaluate(DefectTicket ticket) {
        return Classification.builder()
                .category("Security")
                .subcategory("Security Vulnerability")
                .severity(Severity.HIGH)
                .priority(1)
                .confidenceScore(0.95)
                .reasoning("Security-related keywords detected - requires security team review")
                .classificationSource("RULES")
                .requiresHumanApproval(true)
                .build();
    }

    @Override
    public int priority() {
        return 2;  // High priority, but after critical
    }
}
