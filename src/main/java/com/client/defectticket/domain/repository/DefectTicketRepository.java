package com.client.defectticket.domain.repository;

import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.TicketStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository for DefectTicket entity with DynamoDB Enhanced Client.
 * Provides CRUD operations and GSI queries.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DefectTicketRepository {

    private final DynamoDbTable<DefectTicket> ticketTable;

    /**
     * Save or update a ticket (upsert).
     */
    public DefectTicket save(DefectTicket ticket) {
        ticketTable.putItem(ticket);
        log.debug("Saved ticket: {}", ticket.getTicketId());
        return ticket;
    }

    /**
     * Find ticket by primary key (ticketId).
     */
    public Optional<DefectTicket> findById(String ticketId) {
        Key key = Key.builder().partitionValue(ticketId).build();
        return Optional.ofNullable(ticketTable.getItem(key));
    }

    /**
     * Find all tickets in a batch using batch-index GSI.
     */
    public List<DefectTicket> findByBatchId(String batchId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(batchId).build());

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        return ticketTable.index("batch-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Find all tickets by status using status-index GSI.
     */
    public List<DefectTicket> findByStatus(TicketStatus status) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(status.name()).build());

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        return ticketTable.index("status-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Delete a ticket by ID.
     */
    public void delete(String ticketId) {
        Key key = Key.builder().partitionValue(ticketId).build();
        ticketTable.deleteItem(key);
        log.debug("Deleted ticket: {}", ticketId);
    }

    /**
     * Check if a ticket exists.
     */
    public boolean exists(String ticketId) {
        return findById(ticketId).isPresent();
    }

    /**
     * Scan all tickets (expensive operation, use with caution).
     * Only use for statistics and reporting.
     */
    public List<DefectTicket> scanAll() {
        log.warn("Performing full table scan - this is an expensive operation");
        return ticketTable.scan()
                .items()
                .stream()
                .collect(Collectors.toList());
    }
}
