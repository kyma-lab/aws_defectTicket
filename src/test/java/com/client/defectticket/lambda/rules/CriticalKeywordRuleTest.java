package com.client.defectticket.lambda.rules;

import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.Severity;
import com.client.defectticket.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for CriticalKeywordRule.
 * Tests deterministic rule evaluation logic.
 */
class CriticalKeywordRuleTest {

    private CriticalKeywordRule rule;

    @BeforeEach
    void setUp() {
        rule = new CriticalKeywordRule();
    }

    @Test
    void shouldApplyWhenSecurityBreachKeywordPresent() {
        // Given
        DefectTicket ticket = createTicket("Security breach detected", "Unauthorized access");

        // When/Then
        assertThat(rule.applies(ticket)).isTrue();
    }

    @Test
    void shouldApplyWhenProductionOutageKeywordPresent() {
        // Given
        DefectTicket ticket = createTicket("Production outage", "System is down");

        // When/Then
        assertThat(rule.applies(ticket)).isTrue();
    }

    @Test
    void shouldNotApplyWhenNoKeywordsPresent() {
        // Given
        DefectTicket ticket = createTicket("Minor UI issue", "Button color is wrong");

        // When/Then
        assertThat(rule.applies(ticket)).isFalse();
    }

    @Test
    void shouldEvaluateToCriticalSeverity() {
        // Given
        DefectTicket ticket = createTicket("Data loss detected", "Customer data lost");

        // When
        Classification result = rule.evaluate(ticket);

        // Then
        assertThat(result.getSeverity()).isEqualTo(Severity.CRITICAL);
        assertThat(result.getPriority()).isEqualTo(1);
        assertThat(result.getConfidenceScore()).isEqualTo(1.0);
        assertThat(result.isRequiresHumanApproval()).isTrue();
        assertThat(result.getClassificationSource()).isEqualTo("RULES");
    }

    @Test
    void shouldHaveHighestPriority() {
        // When/Then
        assertThat(rule.priority()).isEqualTo(1);
    }

    private DefectTicket createTicket(String title, String description) {
        return DefectTicket.builder()
                .ticketId("test-123")
                .batchId("batch-001")
                .title(title)
                .description(description)
                .status(TicketStatus.NEW)
                .createdAt(Instant.now())
                .build();
    }
}
