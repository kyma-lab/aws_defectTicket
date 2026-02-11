package com.client.defectticket.lambda.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for batch ingestion Lambda function.
 * Contains ticket IDs for Step Functions processing (Claim Check Pattern).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchIngestionResponse {
    private String batchId;
    private List<String> ticketIds;
    private int successCount;
    private int failureCount;
}
