package com.client.defectticket.lambda.rules;

import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.DefectTicket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for evaluating deterministic classification rules.
 * Rules are evaluated in priority order and can override LLM results.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEvaluationService {

    private final List<ClassificationRule> rules;

    /**
     * Evaluate all rules for a ticket and return the highest priority match.
     * 
     * @param ticket The ticket to evaluate
     * @return Classification from the highest priority matching rule, or empty
     */
    public Optional<Classification> evaluateRules(DefectTicket ticket) {
        log.debug("Evaluating {} rules for ticket: {}", rules.size(), ticket.getTicketId());

        return rules.stream()
                .filter(rule -> {
                    boolean applies = rule.applies(ticket);
                    if (applies) {
                        log.info("Rule {} triggered for ticket {}", 
                                rule.ruleName(), ticket.getTicketId());
                    }
                    return applies;
                })
                .min(Comparator.comparingInt(ClassificationRule::priority))
                .map(rule -> {
                    Classification result = rule.evaluate(ticket);
                    log.info("Applying rule {} classification: category={}, severity={}", 
                            rule.ruleName(), result.getCategory(), result.getSeverity());
                    return result;
                });
    }

    /**
     * Combine LLM result with rule result.
     * Rules take precedence if:
     * 1. Rule severity is higher than LLM severity
     * 2. Rule requires human approval
     * 
     * @param llmResult Classification from AI
     * @param ruleResult Classification from rules
     * @return Combined classification
     */
    public Classification combineWithLlmResult(Classification llmResult, Classification ruleResult) {
        // Rules always override LLM if severity is higher
        if (ruleResult.getSeverity().ordinal() < llmResult.getSeverity().ordinal()) {
            log.info("Rule result overrides LLM: severity {} > {}", 
                    ruleResult.getSeverity(), llmResult.getSeverity());
            return ruleResult;
        }

        // If rule flags for human approval, preserve that flag
        if (ruleResult.isRequiresHumanApproval() && !llmResult.isRequiresHumanApproval()) {
            log.info("Rule requires human approval, updating LLM result");
            llmResult.setRequiresHumanApproval(true);
            llmResult.setClassificationSource("HYBRID");
        }

        return llmResult;
    }

    /**
     * Get all active rules for debugging/monitoring.
     */
    public List<String> listActiveRules() {
        return rules.stream()
                .sorted(Comparator.comparingInt(ClassificationRule::priority))
                .map(ClassificationRule::ruleName)
                .toList();
    }
}
