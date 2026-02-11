package com.client.defectticket.lambda.handler;

import com.client.defectticket.domain.model.AuditEntry;
import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.TicketStatus;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import com.client.defectticket.lambda.classifier.MockClassificationService;
import com.client.defectticket.lambda.classifier.SpringAiClassificationService;
import com.client.defectticket.lambda.handler.dto.ClassificationRequest;
import com.client.defectticket.lambda.handler.dto.ClassificationResponse;
import com.client.defectticket.lambda.rules.RuleEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Lambda handler for ticket classification.
 * Combines AI-based classification with deterministic rules.
 * Implements Claim Check Pattern - loads full ticket from DynamoDB.
 *
 * Uses MockClassificationService in local/test profiles, real Bedrock in production.
 */
@Slf4j
@Component
public class ClassificationHandler {

    private final DefectTicketRepository ticketRepository;
    private final RuleEvaluationService ruleEvaluator;
    
    @Autowired(required = false)
    private SpringAiClassificationService aiClassifier;
    
    @Autowired(required = false)
    private MockClassificationService mockClassifier;

    public ClassificationHandler(DefectTicketRepository ticketRepository,
                                  RuleEvaluationService ruleEvaluator) {
        this.ticketRepository = ticketRepository;
        this.ruleEvaluator = ruleEvaluator;
    }

    /**
     * Classify a ticket using AI and rules.
     * Rules can override AI classification if severity is higher.
     * Uses mock classifier in local/test mode, real Bedrock in production.
     */
    public ClassificationResponse handle(ClassificationRequest request) {
        log.info("Classifying ticket: {}", request.getTicketId());

        // Load full ticket from DynamoDB (Claim Check Pattern)
        DefectTicket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.getTicketId()));

        // AI classification (use mock in local/test, real Bedrock in production)
        Classification aiClassification;
        if (mockClassifier != null) {
            log.info("Using MOCK classifier for local development");
            aiClassification = mockClassifier.classify(ticket);
        } else if (aiClassifier != null) {
            log.info("Using REAL Bedrock classifier");
            aiClassification = aiClassifier.classify(ticket);
        } else {
            throw new IllegalStateException("No classifier available (neither mock nor Bedrock)");
        }
        
        log.info("AI classification for ticket {}: category={}, severity={}, confidence={}", 
                ticket.getTicketId(), aiClassification.getCategory(), 
                aiClassification.getSeverity(), aiClassification.getConfidenceScore());

        // Rule evaluation
        Optional<Classification> ruleClassification = ruleEvaluator.evaluateRules(ticket);

        // Combine AI and rules
        Classification finalClassification;
        if (ruleClassification.isPresent()) {
            finalClassification = ruleEvaluator.combineWithLlmResult(aiClassification, ruleClassification.get());
            log.info("Combined classification for ticket {}: source={}", 
                    ticket.getTicketId(), finalClassification.getClassificationSource());
        } else {
            finalClassification = aiClassification;
        }

        // Update ticket in DynamoDB
        ticket.setClassification(finalClassification);
        ticket.setStatus(TicketStatus.CLASSIFIED);
        ticket.setUpdatedAt(Instant.now());
        
        // Add audit entry
        if (ticket.getAuditTrail() == null) {
            ticket.setAuditTrail(new ArrayList<>());
        }
        ticket.getAuditTrail().add(AuditEntry.builder()
                .fromStatus(TicketStatus.NEW)
                .toStatus(TicketStatus.CLASSIFIED)
                .actor("SYSTEM")
                .reason("AI+Rules classification")
                .timestamp(Instant.now())
                .build());

        ticketRepository.save(ticket);

        log.info("Ticket {} classified and saved: requiresApproval={}", 
                ticket.getTicketId(), finalClassification.isRequiresHumanApproval());

        return ClassificationResponse.builder()
                .ticketId(ticket.getTicketId())
                .classification(finalClassification)
                .build();
    }
}
