package com.client.defectticket.lambda.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for approval creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalCreationResponse {
    private String approvalId;
    private String ticketId;
    private boolean created;
}
