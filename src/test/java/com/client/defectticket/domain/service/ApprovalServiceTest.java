package com.client.defectticket.domain.service;

import com.client.defectticket.api.dto.ApprovalDecisionDto;
import com.client.defectticket.domain.model.ApprovalRequest;
import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.enums.ApprovalGate;
import com.client.defectticket.domain.model.enums.ApprovalStatus;
import com.client.defectticket.domain.model.enums.Severity;
import com.client.defectticket.domain.repository.ApprovalRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for ApprovalService.
 * Tests HITL approval workflow and divergence tracking.
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalRequestRepository approvalRepository;

    @Mock
    private SfnClient sfnClient;

    private ApprovalService approvalService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        approvalService = new ApprovalService(approvalRepository, sfnClient, objectMapper);
        ReflectionTestUtils.setField(approvalService, "trackDivergence", true);
    }

    @Test
    void shouldProcessApprovalAndResumeStepFunctions() throws Exception {
        // Given
        Classification aiClassification = Classification.builder()
                .category("Bug")
                .severity(Severity.MEDIUM)
                .confidenceScore(0.75)
                .requiresHumanApproval(true)
                .build();

        ApprovalRequest approval = ApprovalRequest.builder()
                .approvalId("approval-123")
                .ticketId("ticket-123")
                .gate(ApprovalGate.CLASSIFICATION_REVIEW)
                .status(ApprovalStatus.PENDING)
                .taskToken("task-token-123")
                .aiRecommendation(objectMapper.writeValueAsString(aiClassification))
                .createdAt(Instant.now())
                .build();

        ApprovalDecisionDto decision = new ApprovalDecisionDto();
        decision.setApprovalId("approval-123");
        decision.setApproved(true);
        decision.setReviewerEmail("reviewer@example.com");
        decision.setComments("Looks good");

        when(approvalRepository.findById("approval-123")).thenReturn(Optional.of(approval));

        // When
        approvalService.processDecision(decision);

        // Then
        ArgumentCaptor<ApprovalRequest> savedApproval = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(approvalRepository).save(savedApproval.capture());

        assertThat(savedApproval.getValue().getStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(savedApproval.getValue().getReviewerEmail()).isEqualTo("reviewer@example.com");
        assertThat(savedApproval.getValue().getReviewedAt()).isNotNull();

        verify(sfnClient).sendTaskSuccess(any(SendTaskSuccessRequest.class));
    }

    @Test
    void shouldTrackDivergenceWhenHumanRejects() throws Exception {
        // Given - AI was confident (no approval needed), but human rejects
        Classification aiClassification = Classification.builder()
                .category("Enhancement")
                .severity(Severity.LOW)
                .confidenceScore(0.95)
                .requiresHumanApproval(false)  // AI was confident
                .build();

        ApprovalRequest approval = ApprovalRequest.builder()
                .approvalId("approval-123")
                .ticketId("ticket-123")
                .gate(ApprovalGate.CLASSIFICATION_REVIEW)
                .status(ApprovalStatus.PENDING)
                .taskToken("task-token-123")
                .aiRecommendation(objectMapper.writeValueAsString(aiClassification))
                .createdAt(Instant.now())
                .build();

        ApprovalDecisionDto decision = new ApprovalDecisionDto();
        decision.setApprovalId("approval-123");
        decision.setApproved(false);  // Human rejects
        decision.setReviewerEmail("reviewer@example.com");

        when(approvalRepository.findById("approval-123")).thenReturn(Optional.of(approval));

        // When
        approvalService.processDecision(decision);

        // Then
        ArgumentCaptor<ApprovalRequest> savedApproval = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(approvalRepository).save(savedApproval.capture());

        assertThat(savedApproval.getValue().getAiVsHumanDivergence()).isTrue();
    }
}
