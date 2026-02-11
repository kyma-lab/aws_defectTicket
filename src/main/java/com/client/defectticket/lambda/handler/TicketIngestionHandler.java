package com.client.defectticket.lambda.handler;

import com.client.defectticket.domain.model.AuditEntry;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.TicketStatus;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import com.client.defectticket.lambda.handler.dto.BatchIngestionRequest;
import com.client.defectticket.lambda.handler.dto.BatchIngestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lambda handler for batch ticket ingestion.
 * Stores tickets in DynamoDB and calculates TTL for archival.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketIngestionHandler {

    private final DefectTicketRepository ticketRepository;

    @Value("${batch.ttl-days:90}")
    private int ttlDays;

    /**
     * Process batch ingestion request.
     * Creates tickets in DynamoDB and returns ticket IDs for Step Functions.
     */
    public BatchIngestionResponse handle(BatchIngestionRequest request) {
        log.info("Ingesting batch: {} with {} tickets from {}", 
                request.getBatchId(), request.getTickets().size(), request.getSourceSystem());

        List<String> ticketIds = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        Instant now = Instant.now();

        for (var ticketInput : request.getTickets()) {
            try {
                DefectTicket ticket = DefectTicket.builder()
                        .ticketId(UUID.randomUUID().toString())
                        .batchId(request.getBatchId())
                        .sourceSystem(request.getSourceSystem())
                        .sourceReference(ticketInput.getSourceReference())
                        .title(ticketInput.getTitle())
                        .description(ticketInput.getDescription())
                        .status(TicketStatus.NEW)
                        .auditTrail(List.of(AuditEntry.builder()
                                .fromStatus(null)
                                .toStatus(TicketStatus.NEW)
                                .actor("SYSTEM")
                                .reason("Batch ingestion")
                                .timestamp(now)
                                .build()))
                        .createdAt(now)
                        .updatedAt(now)
                        .ttl(now.plus(ttlDays, ChronoUnit.DAYS).getEpochSecond())
                        .build();

                ticketRepository.save(ticket);
                ticketIds.add(ticket.getTicketId());
                successCount++;

            } catch (Exception e) {
                log.error("Failed to ingest ticket: {}", ticketInput.getSourceReference(), e);
                failureCount++;
            }
        }

        log.info("Batch {} ingestion complete: {} success, {} failures", 
                request.getBatchId(), successCount, failureCount);

        return BatchIngestionResponse.builder()
                .batchId(request.getBatchId())
                .ticketIds(ticketIds)
                .successCount(successCount)
                .failureCount(failureCount)
                .build();
    }
}
