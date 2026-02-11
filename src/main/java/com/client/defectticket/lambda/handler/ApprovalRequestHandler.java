package com.client.defectticket.lambda.handler;

import com.client.defectticket.domain.model.ApprovalRequest;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.ApprovalStatus;
import com.client.defectticket.domain.repository.ApprovalRequestRepository;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import com.client.defectticket.lambda.handler.dto.ApprovalCreationRequest;
import com.client.defectticket.lambda.handler.dto.ApprovalCreationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lambda handler for creating HITL approval requests.
 * Pauses Step Functions execution until manual approval.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalRequestHandler {

    private final ApprovalRequestRepository approvalRepository;
    private final DefectTicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    @Value("${hitl.approval.timeout-hours:24}")
    private int timeoutHours;

    /**
     * Create an approval request and pause workflow.
     * Stores AI recommendation for divergence tracking.
     */
    public ApprovalCreationResponse handle(ApprovalCreationRequest request) {
        log.info("Creating approval request for ticket {} at gate {}", 
                request.getTicketId(), request.getGate());

        DefectTicket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.getTicketId()));

        Instant now = Instant.now();
        String approvalId = UUID.randomUUID().toString();

        // Build context with ticket and classification info
        String context = buildApprovalContext(ticket);

        // Store AI recommendation for later divergence tracking
        String aiRecommendation = serializeAiRecommendation(ticket);

        ApprovalRequest approval = ApprovalRequest.builder()
                .approvalId(approvalId)
                .ticketId(request.getTicketId())
                .gate(request.getGate())
                .status(ApprovalStatus.PENDING)
                .taskToken(request.getTaskToken())
                .context(context)
                .aiRecommendation(aiRecommendation)
                .aiVsHumanDivergence(null)  // Will be set when human decides
                .createdAt(now)
                .expiresAt(now.plus(timeoutHours, ChronoUnit.HOURS))
                .build();

        approvalRepository.save(approval);

        log.info("Approval request {} created for ticket {}, expires at {}", 
                approvalId, ticket.getTicketId(), approval.getExpiresAt());

        return ApprovalCreationResponse.builder()
                .approvalId(approvalId)
                .ticketId(request.getTicketId())
                .created(true)
                .build();
    }

    private String buildApprovalContext(DefectTicket ticket) {
        try {
            Map<String, Object> context = new HashMap<>();
            context.put("ticketId", ticket.getTicketId());
            context.put("title", ticket.getTitle());
            context.put("description", ticket.getDescription());
            context.put("sourceSystem", ticket.getSourceSystem());
            context.put("sourceReference", ticket.getSourceReference());
            
            if (ticket.getClassification() != null) {
                context.put("category", ticket.getClassification().getCategory());
                context.put("severity", ticket.getClassification().getSeverity());
                context.put("priority", ticket.getClassification().getPriority());
                context.put("confidence", ticket.getClassification().getConfidenceScore());
                context.put("reasoning", ticket.getClassification().getReasoning());
            }
            
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize approval context", e);
            return "{}";
        }
    }

    private String serializeAiRecommendation(DefectTicket ticket) {
        try {
            return objectMapper.writeValueAsString(ticket.getClassification());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize AI recommendation", e);
            return null;
        }
    }
}
