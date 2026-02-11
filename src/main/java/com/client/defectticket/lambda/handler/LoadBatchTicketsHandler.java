package com.client.defectticket.lambda.handler;

import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lambda handler to load ticket IDs from DynamoDB by batchId.
 * Implements Claim Check Pattern for Step Functions.
 * 
 * This prevents 256KB payload limit issues by only passing IDs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoadBatchTicketsHandler {

    private final DefectTicketRepository ticketRepository;

    /**
     * Load all ticket IDs for a batch.
     * Returns list of IDs for Map state processing.
     */
    public List<String> handle(Map<String, String> input) {
        String batchId = input.get("batchId");
        log.info("Loading ticket IDs for batch: {}", batchId);

        List<DefectTicket> tickets = ticketRepository.findByBatchId(batchId);
        
        List<String> ticketIds = tickets.stream()
                .map(DefectTicket::getTicketId)
                .collect(Collectors.toList());

        log.info("Loaded {} ticket IDs for batch {}", ticketIds.size(), batchId);

        return ticketIds;
    }
}
