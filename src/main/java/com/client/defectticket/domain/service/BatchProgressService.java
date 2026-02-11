package com.client.defectticket.domain.service;

import com.client.defectticket.api.dto.BatchProgressDto;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.TicketStatus;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for tracking batch processing progress.
 * Provides real-time progress updates for HITL dashboard.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProgressService {

    private final DefectTicketRepository ticketRepository;

    /**
     * Calculate batch processing progress.
     * Uses GSI queries for efficient status breakdown.
     */
    public BatchProgressDto calculateProgress(String batchId) {
        log.debug("Calculating progress for batch: {}", batchId);

        List<DefectTicket> tickets = ticketRepository.findByBatchId(batchId);
        
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("Batch not found: " + batchId);
        }

        int totalTickets = tickets.size();
        Map<String, Integer> statusBreakdown = new HashMap<>();
        int processedCount = 0;

        for (DefectTicket ticket : tickets) {
            String status = ticket.getStatus().name();
            statusBreakdown.merge(status, 1, Integer::sum);

            // Count tickets that are beyond initial classification
            if (isProcessed(ticket.getStatus())) {
                processedCount++;
            }
        }

        double progressPercentage = (totalTickets > 0) 
            ? (processedCount * 100.0 / totalTickets) 
            : 0.0;

        BatchProgressDto progress = BatchProgressDto.builder()
                .batchId(batchId)
                .totalTickets(totalTickets)
                .processedTickets(processedCount)
                .pendingTickets(totalTickets - processedCount)
                .statusBreakdown(statusBreakdown)
                .progressPercentage(Math.round(progressPercentage * 100.0) / 100.0)
                .build();

        log.info("Batch {} progress: {}/{} tickets processed ({} %)", 
                batchId, processedCount, totalTickets, progress.getProgressPercentage());

        return progress;
    }

    private boolean isProcessed(TicketStatus status) {
        return status == TicketStatus.CLASSIFIED 
            || status == TicketStatus.CLASSIFICATION_APPROVED
            || status == TicketStatus.ASSIGNED
            || status == TicketStatus.IN_PROGRESS
            || status == TicketStatus.RESOLVED
            || status == TicketStatus.CLOSED
            || status == TicketStatus.ARCHIVED;
    }
}
