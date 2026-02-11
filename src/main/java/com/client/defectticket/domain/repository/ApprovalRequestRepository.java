package com.client.defectticket.domain.repository;

import com.client.defectticket.domain.model.ApprovalRequest;
import com.client.defectticket.domain.model.enums.ApprovalStatus;
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
 * Repository for ApprovalRequest entity.
 * Supports querying by ticket and status for HITL dashboard.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ApprovalRequestRepository {

    private final DynamoDbTable<ApprovalRequest> approvalTable;

    /**
     * Save or update an approval request.
     */
    public ApprovalRequest save(ApprovalRequest request) {
        approvalTable.putItem(request);
        log.debug("Saved approval request: {}", request.getApprovalId());
        return request;
    }

    /**
     * Find approval request by ID.
     */
    public Optional<ApprovalRequest> findById(String approvalId) {
        Key key = Key.builder().partitionValue(approvalId).build();
        return Optional.ofNullable(approvalTable.getItem(key));
    }

    /**
     * Find all approval requests for a ticket using ticket-index GSI.
     */
    public List<ApprovalRequest> findByTicketId(String ticketId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(ticketId).build());

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        return approvalTable.index("ticket-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Find all approval requests by status using status-index GSI.
     * Critical for HITL dashboard to show pending approvals.
     */
    public List<ApprovalRequest> findByStatus(ApprovalStatus status) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(status.name()).build());

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        return approvalTable.index("status-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Check if an approval exists.
     */
    public boolean exists(String approvalId) {
        return findById(approvalId).isPresent();
    }
}
