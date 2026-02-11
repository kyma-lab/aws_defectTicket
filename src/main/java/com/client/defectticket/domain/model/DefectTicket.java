package com.client.defectticket.domain.model;

import com.client.defectticket.domain.model.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entity representing a defect ticket.
 * Uses DynamoDB Enhanced Client annotations for table mapping.
 * 
 * GSIs:
 * - batch-index: Query tickets by batchId
 * - status-index: Query tickets by status
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class DefectTicket {

    private String ticketId;
    private String batchId;
    private String sourceSystem;
    private String sourceReference;
    private String title;
    private String description;
    private TicketStatus status;
    private Classification classification;
    private Assignment assignment;

    @Builder.Default
    private List<AuditEntry> auditTrail = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    public String getTicketId() {
        return ticketId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "batch-index")
    public String getBatchId() {
        return batchId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "status-index")
    public TicketStatus getStatus() {
        return status;
    }

    /**
     * Version field for optimistic locking.
     * DynamoDB Enhanced Client handles this automatically.
     */
    private Long version;

    /**
     * TTL attribute for automatic archival.
     * Set to 90 days after creation (configurable via batch.ttl-days).
     */
    private Long ttl;
}
