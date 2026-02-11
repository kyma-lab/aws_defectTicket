package com.client.defectticket.api.controller;

import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.TicketStatus;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import com.client.defectticket.lambda.handler.dto.BatchIngestionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST API controller for batch ingestion.
 * Stores tickets in DynamoDB and sends message to SQS.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/batch-ingestion")
@RequiredArgsConstructor
public class BatchIngestionController {

    private final DefectTicketRepository ticketRepository;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.ingestion-queue-url}")
    private String queueUrl;

    /**
     * Submit a batch of defect tickets for processing.
     * 
     * 1. Stores full ticket data in DynamoDB (Claim Check Pattern)
     * 2. Sends lightweight message to SQS queue
     * 3. SQS consumer triggers Step Functions workflow
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestBatch(@RequestBody BatchIngestionRequest request) {
        log.info("Ingesting batch: {} with {} tickets", request.getBatchId(), request.getTickets().size());

        // Step 1: Store full ticket data in DynamoDB (Claim Check Pattern)
        int storedCount = 0;
        for (BatchIngestionRequest.TicketInput ticketInput : request.getTickets()) {
            DefectTicket ticket = new DefectTicket();
            ticket.setTicketId(UUID.randomUUID().toString());
            ticket.setBatchId(request.getBatchId());
            ticket.setSourceSystem(request.getSourceSystem());
            ticket.setSourceReference(ticketInput.getSourceReference());
            ticket.setTitle(ticketInput.getTitle());
            ticket.setDescription(ticketInput.getDescription());
            ticket.setStatus(TicketStatus.NEW);
            ticket.setCreatedAt(Instant.now());
            ticket.setUpdatedAt(Instant.now());
            
            ticketRepository.save(ticket);
            storedCount++;
            log.debug("Stored ticket: {} from {}", ticket.getTicketId(), ticketInput.getSourceReference());
        }

        log.info("Stored {} tickets in DynamoDB for batch: {}", storedCount, request.getBatchId());

        // Step 2: Send lightweight message to SQS
        try {
            String messageBody = objectMapper.writeValueAsString(request);
            
            SendMessageRequest sqsRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();
            
            var sqsResponse = sqsClient.sendMessage(sqsRequest);
            
            log.info("Sent SQS message {} for batch: {}", sqsResponse.messageId(), request.getBatchId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("batchId", request.getBatchId());
            response.put("ticketsIngested", storedCount);
            response.put("sqsMessageId", sqsResponse.messageId());
            response.put("status", "QUEUED");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Failed to send SQS message for batch: {}", request.getBatchId(), e);
            throw new RuntimeException("Failed to queue batch for processing", e);
        }
    }
}
