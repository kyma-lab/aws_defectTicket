package com.client.defectticket.domain.model;

import com.client.defectticket.domain.model.enums.ApprovalGate;
import com.client.defectticket.domain.model.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;

/**
 * HITL approval request entity.
 * Tracks when humans need to review and approve AI decisions.
 * 
 * GSIs:
 * - ticket-index: Query approvals by ticketId
 * - status-index: Query approvals by status (e.g., PENDING)
 * 
 * New fields for feedback tracking:
 * - aiVsHumanDivergence: Flags when human overrides AI decision
 * - aiRecommendation: Stores original AI classification for comparison
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class ApprovalRequest {

    private String approvalId;
    private String ticketId;
    private ApprovalGate gate;
    private ApprovalStatus status;

    /**
     * Step Functions task token for callback pattern.
     * Used to resume workflow after manual approval.
     */
    private String taskToken;

    /**
     * JSON context payload with ticket and classification data.
     * Lightweight reference following Claim Check Pattern.
     */
    private String context;

    private String reviewerEmail;
    private String reviewerComments;
    private Instant reviewedAt;

    /**
     * Track AI vs Human divergence for ML training and compliance.
     */
    private Boolean aiVsHumanDivergence;

    /**
     * Original AI classification for comparison with human decision.
     */
    private String aiRecommendation;

    @DynamoDbPartitionKey
    public String getApprovalId() {
        return approvalId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "ticket-index")
    public String getTicketId() {
        return ticketId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "status-index")
    public ApprovalStatus getStatus() {
        return status;
    }

    private Instant createdAt;
    private Instant expiresAt;

    /**
     * Version field for optimistic locking.
     * DynamoDB Enhanced Client handles this automatically.
     */
    private Long version;
}
