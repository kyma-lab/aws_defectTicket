package com.client.defectticket.lambda.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for batch ingestion Lambda function.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchIngestionRequest {
    private String batchId;
    private String sourceSystem;
    private List<TicketInput> tickets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketInput {
        private String sourceReference;
        private String title;
        private String description;
    }
}
