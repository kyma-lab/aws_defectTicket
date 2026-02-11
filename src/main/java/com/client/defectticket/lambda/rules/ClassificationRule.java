package com.client.defectticket.lambda.rules;

import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.DefectTicket;

/**
 * Interface for deterministic classification rules.
 * Rules are evaluated in priority order (lower number = higher priority).
 */
public interface ClassificationRule {
    
    /**
     * Check if this rule applies to the given ticket.
     */
    boolean applies(DefectTicket ticket);
    
    /**
     * Evaluate the rule and return classification.
     */
    Classification evaluate(DefectTicket ticket);
    
    /**
     * Priority of this rule (lower = higher priority).
     * Critical rules (e.g., security keywords) should have priority 1.
     */
    int priority();
    
    /**
     * Human-readable name for logging and debugging.
     */
    default String ruleName() {
        return this.getClass().getSimpleName();
    }
}
