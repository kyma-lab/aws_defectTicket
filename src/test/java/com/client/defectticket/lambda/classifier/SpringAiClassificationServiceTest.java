package com.client.defectticket.lambda.classifier;

import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.Severity;
import com.client.defectticket.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.bedrock.anthropic.BedrockAnthropicChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for SpringAiClassificationService.
 * Mocks Bedrock calls to test classification logic.
 */
@ExtendWith(MockitoExtension.class)
class SpringAiClassificationServiceTest {

    @Mock
    private BedrockAnthropicChatModel chatModel;

    private SpringAiClassificationService classificationService;

    @BeforeEach
    void setUp() {
        classificationService = new SpringAiClassificationService(chatModel);
        ReflectionTestUtils.setField(classificationService, "confidenceThreshold", 0.85);
    }

    @Test
    void shouldClassifyTicketWithHighConfidence() {
        // Given
        DefectTicket ticket = createTestTicket("Critical security vulnerability", 
            "SQL injection found in login form");

        String llmResponse = """
            {
              "category": "Security",
              "subcategory": "SQL Injection",
              "severity": "CRITICAL",
              "priority": 1,
              "confidenceScore": 0.95,
              "reasoning": "Critical security vulnerability requiring immediate attention"
            }
            """;

        mockLlmResponse(llmResponse);

        // When
        Classification result = classificationService.classify(ticket);

        // Then
        assertThat(result.getCategory()).isEqualTo("Security");
        assertThat(result.getSeverity()).isEqualTo(Severity.CRITICAL);
        assertThat(result.getConfidenceScore()).isEqualTo(0.95);
        assertThat(result.isRequiresHumanApproval()).isFalse();  // High confidence
        assertThat(result.getClassificationSource()).isEqualTo("LLM");
    }

    @Test
    void shouldRequireHumanApprovalForLowConfidence() {
        // Given
        DefectTicket ticket = createTestTicket("Minor UI issue", "Button alignment is off");

        String llmResponse = """
            {
              "category": "UI/UX",
              "subcategory": "Cosmetic",
              "severity": "LOW",
              "priority": 4,
              "confidenceScore": 0.70,
              "reasoning": "Minor cosmetic issue"
            }
            """;

        mockLlmResponse(llmResponse);

        // When
        Classification result = classificationService.classify(ticket);

        // Then
        assertThat(result.getConfidenceScore()).isEqualTo(0.70);
        assertThat(result.isRequiresHumanApproval()).isTrue();  // Low confidence < 0.85
    }

    @Test
    void shouldThrowLlmServiceExceptionOnFailure() {
        // Given
        DefectTicket ticket = createTestTicket("Test", "Test");
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("Bedrock error"));

        // When/Then
        assertThatThrownBy(() -> classificationService.classify(ticket))
                .isInstanceOf(LlmServiceException.class)
                .hasMessageContaining("Failed to classify ticket");
    }

    private DefectTicket createTestTicket(String title, String description) {
        return DefectTicket.builder()
                .ticketId("test-123")
                .batchId("batch-001")
                .sourceSystem("TEST")
                .title(title)
                .description(description)
                .status(TicketStatus.NEW)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private void mockLlmResponse(String jsonResponse) {
        Generation generation = new Generation(new AssistantMessage(jsonResponse));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    }
}
