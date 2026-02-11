package com.client.defectticket.api.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * DTO for batch processing progress.
 * Shows overall progress and ticket status breakdown.
 */
@Value
@Builder
public class BatchProgressDto {
    String batchId;
    int totalTickets;
    int processedTickets;
    int pendingTickets;
    Map<String, Integer> statusBreakdown;
    double progressPercentage;
}
