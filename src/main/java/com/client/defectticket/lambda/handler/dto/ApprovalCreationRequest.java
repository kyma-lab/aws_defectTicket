package com.client.defectticket.lambda.handler.dto;

import com.client.defectticket.domain.model.enums.ApprovalGate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating approval requests.
 * Includes task token for Step Functions callback pattern.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalCreationRequest {
    private String ticketId;
    private ApprovalGate gate;
    private String taskToken;  // Step Functions task token
    private String context;    // JSON context for dashboard
}
