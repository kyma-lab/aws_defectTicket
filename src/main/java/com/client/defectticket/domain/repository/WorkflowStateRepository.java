package com.client.defectticket.domain.repository;

import com.client.defectticket.domain.model.WorkflowState;
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
 * Repository for WorkflowState entity.
 * Tracks Step Functions execution state.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class WorkflowStateRepository {

    private final DynamoDbTable<WorkflowState> workflowStateTable;

    /**
     * Save or update workflow state.
     */
    public WorkflowState save(WorkflowState state) {
        workflowStateTable.putItem(state);
        log.debug("Saved workflow state: {}", state.getExecutionId());
        return state;
    }

    /**
     * Find workflow state by execution ID.
     */
    public Optional<WorkflowState> findById(String executionId) {
        Key key = Key.builder().partitionValue(executionId).build();
        return Optional.ofNullable(workflowStateTable.getItem(key));
    }

    /**
     * Find all workflow states for a batch using batch-index GSI.
     */
    public List<WorkflowState> findByBatchId(String batchId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(batchId).build());

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();

        return workflowStateTable.index("batch-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    /**
     * Check if workflow state exists.
     */
    public boolean exists(String executionId) {
        return findById(executionId).isPresent();
    }
}
