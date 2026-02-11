package com.client.defectticket.lambda.handler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for classification Lambda function.
 * Only contains ticketId (Claim Check Pattern).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationRequest {
    private String ticketId;
}
