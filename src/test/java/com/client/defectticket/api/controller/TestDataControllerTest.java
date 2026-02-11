package com.client.defectticket.api.controller;

import com.client.defectticket.domain.model.ApprovalRequest;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.ApprovalGate;
import com.client.defectticket.domain.model.enums.ApprovalStatus;
import com.client.defectticket.domain.model.enums.TicketStatus;
import com.client.defectticket.domain.repository.ApprovalRequestRepository;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TestDataController.
 * Tests local development test data seeding functionality.
 */
@ExtendWith(MockitoExtension.class)
class TestDataControllerTest {

    @Mock
    private DefectTicketRepository ticketRepository;

    @Mock
    private ApprovalRequestRepository approvalRepository;

    private TestDataController controller;

    @BeforeEach
    void setUp() {
        controller = new TestDataController(ticketRepository, approvalRepository);
    }

    @Test
    void shouldSeedTestDataSuccessfully() {
        // When
        ResponseEntity<Map<String, Object>> response = controller.seedTestData();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Test data seeded successfully");
        
        // Verify tickets array
        String[] tickets = (String[]) response.getBody().get("tickets");
        assertThat(tickets).hasSize(2);
        assertThat(tickets).contains("ticket-test-001", "ticket-test-002");
        
        // Verify approvals array
        String[] approvals = (String[]) response.getBody().get("approvals");
        assertThat(approvals).hasSize(2);
        assertThat(approvals).contains(
            "approval-12345678-abcd-1234-efgh-123456789012",
            "approval-87654321-dcba-4321-hgfe-210987654321"
        );
        
        // Verify batch ID
        assertThat(response.getBody().get("batchId")).isEqualTo("batch-test-001");
    }

    @Test
    void shouldCreateTwoDefectTickets() {
        // When
        controller.seedTestData();

        // Then
        ArgumentCaptor<DefectTicket> ticketCaptor = ArgumentCaptor.forClass(DefectTicket.class);
        verify(ticketRepository, times(2)).save(ticketCaptor.capture());
        
        DefectTicket ticket1 = ticketCaptor.getAllValues().get(0);
        assertThat(ticket1.getTicketId()).isEqualTo("ticket-test-001");
        assertThat(ticket1.getBatchId()).isEqualTo("batch-test-001");
        assertThat(ticket1.getSourceSystem()).isEqualTo("JIRA");
        assertThat(ticket1.getTitle()).contains("Critical login bug");
        assertThat(ticket1.getStatus()).isEqualTo(TicketStatus.NEW);
        assertThat(ticket1.getCreatedAt()).isNotNull();
        assertThat(ticket1.getUpdatedAt()).isNotNull();
        
        DefectTicket ticket2 = ticketCaptor.getAllValues().get(1);
        assertThat(ticket2.getTicketId()).isEqualTo("ticket-test-002");
        assertThat(ticket2.getTitle()).contains("UI button alignment");
        assertThat(ticket2.getStatus()).isEqualTo(TicketStatus.NEW);
    }

    @Test
    void shouldCreateTwoApprovalRequests() {
        // When
        controller.seedTestData();

        // Then
        ArgumentCaptor<ApprovalRequest> approvalCaptor = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(approvalRepository, times(2)).save(approvalCaptor.capture());
        
        ApprovalRequest approval1 = approvalCaptor.getAllValues().get(0);
        assertThat(approval1.getApprovalId()).isEqualTo("approval-12345678-abcd-1234-efgh-123456789012");
        assertThat(approval1.getTicketId()).isEqualTo("ticket-test-001");
        assertThat(approval1.getGate()).isEqualTo(ApprovalGate.CLASSIFICATION_REVIEW);
        assertThat(approval1.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(approval1.getTaskToken()).startsWith("test-task-token-");
        assertThat(approval1.getContext()).contains("CRITICAL");
        assertThat(approval1.getAiRecommendation()).contains("requiresHumanApproval");
        assertThat(approval1.getCreatedAt()).isNotNull();
        assertThat(approval1.getExpiresAt()).isNotNull();
        
        ApprovalRequest approval2 = approvalCaptor.getAllValues().get(1);
        assertThat(approval2.getApprovalId()).isEqualTo("approval-87654321-dcba-4321-hgfe-210987654321");
        assertThat(approval2.getTicketId()).isEqualTo("ticket-test-002");
        assertThat(approval2.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(approval2.getContext()).contains("LOW");
    }

    @Test
    void shouldSetExpirationTo24HoursFromNow() {
        // When
        controller.seedTestData();

        // Then
        ArgumentCaptor<ApprovalRequest> approvalCaptor = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(approvalRepository, times(2)).save(approvalCaptor.capture());
        
        for (ApprovalRequest approval : approvalCaptor.getAllValues()) {
            long expirationSeconds = approval.getExpiresAt().getEpochSecond() - approval.getCreatedAt().getEpochSecond();
            assertThat(expirationSeconds).isEqualTo(86400); // 24 hours
        }
    }

    @Test
    void shouldIncludeValidJsonContext() {
        // When
        controller.seedTestData();

        // Then
        ArgumentCaptor<ApprovalRequest> approvalCaptor = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(approvalRepository, times(2)).save(approvalCaptor.capture());
        
        for (ApprovalRequest approval : approvalCaptor.getAllValues()) {
            assertThat(approval.getContext()).startsWith("{");
            assertThat(approval.getContext()).endsWith("}");
            assertThat(approval.getContext()).contains("ticket");
            assertThat(approval.getContext()).contains("classification");
        }
    }

    @Test
    void shouldIncludeValidAiRecommendation() {
        // When
        controller.seedTestData();

        // Then
        ArgumentCaptor<ApprovalRequest> approvalCaptor = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(approvalRepository, times(2)).save(approvalCaptor.capture());
        
        for (ApprovalRequest approval : approvalCaptor.getAllValues()) {
            assertThat(approval.getAiRecommendation()).contains("severity");
            assertThat(approval.getAiRecommendation()).contains("confidence");
            assertThat(approval.getAiRecommendation()).contains("requiresHumanApproval");
            assertThat(approval.getAiRecommendation()).contains("reasoning");
        }
    }

    @Test
    void shouldReturnOkForClearTestData() {
        // When
        ResponseEntity<Map<String, String>> response = controller.clearTestData();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).contains("Test data cleared");
    }

    @Test
    void shouldGenerateTaskTokensWithCorrectFormat() {
        // When
        controller.seedTestData();

        // Then
        ArgumentCaptor<ApprovalRequest> approvalCaptor = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(approvalRepository, times(2)).save(approvalCaptor.capture());
        
        String token1 = approvalCaptor.getAllValues().get(0).getTaskToken();
        String token2 = approvalCaptor.getAllValues().get(1).getTaskToken();
        
        // Both tokens should have correct format
        assertThat(token1).startsWith("test-task-token-");
        assertThat(token2).startsWith("test-task-token-");
        
        // Tokens should not be null or empty
        assertThat(token1).isNotNull().isNotEmpty();
        assertThat(token2).isNotNull().isNotEmpty();
    }
}
