package com.client.defectticket.api.controller;

import com.client.defectticket.domain.model.ApprovalRequest;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.ApprovalGate;
import com.client.defectticket.domain.model.enums.ApprovalStatus;
import com.client.defectticket.domain.model.enums.Severity;
import com.client.defectticket.domain.model.enums.TicketStatus;
import com.client.defectticket.domain.repository.ApprovalRequestRepository;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Test data controller for local development.
 * Only available in 'local' profile.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Profile("local")
public class TestDataController {

    private final DefectTicketRepository ticketRepository;
    private final ApprovalRequestRepository approvalRepository;

    /**
     * Create sample test data for local testing.
     */
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedTestData() {
        log.info("Seeding test data for local development");

        // Create test defect ticket
        DefectTicket ticket = new DefectTicket();
        ticket.setTicketId("ticket-test-001");
        ticket.setBatchId("batch-test-001");
        ticket.setSourceSystem("JIRA");
        ticket.setTitle("Critical login bug causing application crash");
        ticket.setDescription("Users are unable to login. Application crashes immediately when attempting authentication. " +
                "This affects all users across production environment. Error logs show NullPointerException in auth module.");
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        
        ticketRepository.save(ticket);
        log.info("Created test ticket: {}", ticket.getTicketId());

        // Create test approval request
        ApprovalRequest approval = new ApprovalRequest();
        approval.setApprovalId("approval-12345678-abcd-1234-efgh-123456789012");
        approval.setTicketId(ticket.getTicketId());
        approval.setGate(ApprovalGate.CLASSIFICATION_REVIEW);
        approval.setStatus(ApprovalStatus.PENDING);
        approval.setTaskToken("test-task-token-" + System.currentTimeMillis());
        
        // Create context JSON
        String context = String.format(
            "{\"ticket\":{\"id\":\"%s\",\"title\":\"%s\",\"severity\":\"%s\"},\"classification\":{\"severity\":\"%s\",\"confidence\":0.92}}",
            ticket.getTicketId(), ticket.getTitle(), "CRITICAL", "CRITICAL"
        );
        approval.setContext(context);
        
        // Set AI recommendation
        String aiRecommendation = "{\"severity\":\"CRITICAL\",\"confidence\":0.92,\"requiresHumanApproval\":true,\"reasoning\":\"High severity classification with security keywords detected\"}";
        approval.setAiRecommendation(aiRecommendation);
        
        approval.setCreatedAt(Instant.now());
        approval.setExpiresAt(Instant.now().plusSeconds(86400)); // 24 hours
        
        approvalRepository.save(approval);
        log.info("Created test approval: {}", approval.getApprovalId());

        // Create second test ticket and approval with different severity
        DefectTicket ticket2 = new DefectTicket();
        ticket2.setTicketId("ticket-test-002");
        ticket2.setBatchId("batch-test-001");
        ticket2.setSourceSystem("JIRA");
        ticket2.setTitle("UI button alignment issue on dashboard");
        ticket2.setDescription("Submit button on the user dashboard is slightly misaligned on mobile devices. " +
                "Cosmetic issue that doesn't affect functionality but impacts user experience.");
        ticket2.setStatus(TicketStatus.NEW);
        ticket2.setCreatedAt(Instant.now());
        ticket2.setUpdatedAt(Instant.now());
        
        ticketRepository.save(ticket2);

        ApprovalRequest approval2 = new ApprovalRequest();
        approval2.setApprovalId("approval-87654321-dcba-4321-hgfe-210987654321");
        approval2.setTicketId(ticket2.getTicketId());
        approval2.setGate(ApprovalGate.CLASSIFICATION_REVIEW);
        approval2.setStatus(ApprovalStatus.PENDING);
        approval2.setTaskToken("test-task-token-" + System.currentTimeMillis());
        
        String context2 = String.format(
            "{\"ticket\":{\"id\":\"%s\",\"title\":\"%s\",\"severity\":\"%s\"},\"classification\":{\"severity\":\"%s\",\"confidence\":0.78}}",
            ticket2.getTicketId(), ticket2.getTitle(), "LOW", "LOW"
        );
        approval2.setContext(context2);
        
        String aiRecommendation2 = "{\"severity\":\"LOW\",\"confidence\":0.78,\"requiresHumanApproval\":true,\"reasoning\":\"Low severity cosmetic issue\"}";
        approval2.setAiRecommendation(aiRecommendation2);
        
        approval2.setCreatedAt(Instant.now());
        approval2.setExpiresAt(Instant.now().plusSeconds(86400));
        
        approvalRepository.save(approval2);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Test data seeded successfully");
        response.put("tickets", new String[]{ticket.getTicketId(), ticket2.getTicketId()});
        response.put("approvals", new String[]{approval.getApprovalId(), approval2.getApprovalId()});
        response.put("batchId", "batch-test-001");

        return ResponseEntity.ok(response);
    }

    /**
     * Clear all test data.
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearTestData() {
        log.info("Clearing test data");
        
        // Note: In a real implementation, you'd want to selectively delete test data
        // For now, just log the intent
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test data cleared (implementation pending)");
        
        return ResponseEntity.ok(response);
    }
}
