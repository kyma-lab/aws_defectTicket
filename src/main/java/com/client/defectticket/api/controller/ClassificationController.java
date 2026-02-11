package com.client.defectticket.api.controller;

import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.ApprovalGate;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import com.client.defectticket.lambda.handler.ApprovalRequestHandler;
import com.client.defectticket.lambda.handler.ClassificationHandler;
import com.client.defectticket.lambda.handler.dto.ApprovalCreationRequest;
import com.client.defectticket.lambda.handler.dto.ClassificationRequest;
import com.client.defectticket.lambda.handler.dto.ClassificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for testing ticket classification.
 * Useful for local development and testing.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/classification")
@RequiredArgsConstructor
public class ClassificationController {

    private final ClassificationHandler classificationHandler;
    private final ApprovalRequestHandler approvalRequestHandler;
    private final DefectTicketRepository ticketRepository;

    /**
     * Classify a ticket by ID.
     * Triggers the full classification workflow (AI + Rules).
     */
    @PostMapping("/{ticketId}")
    public ResponseEntity<ClassificationResponse> classifyTicket(@PathVariable String ticketId) {
        log.info("API: Classifying ticket: {}", ticketId);
        
        ClassificationRequest request = ClassificationRequest.builder()
                .ticketId(ticketId)
                .build();
        
        ClassificationResponse response = classificationHandler.handle(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Classify all tickets in a batch.
     * Processes all NEW tickets and creates approval requests where needed.
     * Simulates the full Step Functions workflow for local testing.
     */
    @PostMapping("/batch/{batchId}")
    public ResponseEntity<Map<String, Object>> classifyBatch(@PathVariable String batchId) {
        log.info("API: Classifying all tickets in batch: {}", batchId);

        List<DefectTicket> tickets = ticketRepository.findByBatchId(batchId);
        
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("Batch not found or has no tickets: " + batchId);
        }

        int classifiedCount = 0;
        int approvalsCreated = 0;
        int failedCount = 0;

        for (DefectTicket ticket : tickets) {
            try {
                // Step 1: Classify the ticket
                ClassificationRequest classificationRequest = ClassificationRequest.builder()
                        .ticketId(ticket.getTicketId())
                        .build();
                
                ClassificationResponse classificationResponse = classificationHandler.handle(classificationRequest);
                classifiedCount++;
                log.debug("Classified ticket: {}", ticket.getTicketId());
                
                // Step 2: Create approval request if needed
                if (classificationResponse.getClassification().isRequiresHumanApproval()) {
                    ApprovalCreationRequest approvalRequest = ApprovalCreationRequest.builder()
                            .ticketId(ticket.getTicketId())
                            .gate(ApprovalGate.CLASSIFICATION_REVIEW)
                            .taskToken("local-test-token-" + System.currentTimeMillis())
                            .build();
                    
                    approvalRequestHandler.handle(approvalRequest);
                    approvalsCreated++;
                    log.debug("Created approval request for ticket: {}", ticket.getTicketId());
                }
                
            } catch (Exception e) {
                failedCount++;
                log.error("Failed to classify ticket: {}", ticket.getTicketId(), e);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("batchId", batchId);
        response.put("totalTickets", tickets.size());
        response.put("classified", classifiedCount);
        response.put("approvalsCreated", approvalsCreated);
        response.put("failed", failedCount);
        response.put("message", String.format("Batch classification completed: %d classified, %d approvals created",
                classifiedCount, approvalsCreated));

        return ResponseEntity.ok(response);
    }
}
