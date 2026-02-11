package com.client.defectticket.lambda.handler.dto;

import com.client.defectticket.domain.model.Classification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for classification Lambda function.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResponse {
    private String ticketId;
    private Classification classification;
}
