package com.client.defectticket.domain.model;

import com.client.defectticket.domain.model.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.time.Instant;

/**
 * Audit trail entry for ticket status changes.
 * Tracks who changed what and when for compliance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class AuditEntry {
    private TicketStatus fromStatus;
    private TicketStatus toStatus;
    private String actor;  // User or system identifier
    private String reason;
    private Instant timestamp;
}
