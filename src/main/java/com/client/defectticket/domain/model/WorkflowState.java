package com.client.defectticket.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.Instant;
import java.util.Map;

/**
 * Workflow state entity for tracking Step Functions execution.
 * 
 * GSI:
 * - batch-index: Query workflow states by batchId
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class WorkflowState {

    private String executionId;
    private String batchId;
    private String executionArn;
    private String currentStep;
    private String status;  // RUNNING, PAUSED, COMPLETED, FAILED

    /**
     * Checkpoint data for resuming workflow.
     * Stores intermediate state for HITL gates.
     */
    private Map<String, String> checkpointData;

    @DynamoDbPartitionKey
    public String getExecutionId() {
        return executionId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "batch-index")
    public String getBatchId() {
        return batchId;
    }

    private Instant startedAt;
    private Instant updatedAt;
    private Instant completedAt;

    /**
     * Version field for optimistic locking.
     * DynamoDB Enhanced Client handles this automatically.
     */
    private Long version;
}
