package com.client.defectticket.domain.model;

import com.client.defectticket.domain.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Classification result for a defect ticket.
 * Contains both AI-based and rule-based classification information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Classification {
    private String category;
    private String subcategory;
    private Severity severity;
    private Integer priority;
    private Double confidenceScore;
    private String reasoning;
    private String classificationSource;  // "LLM", "RULES", "HYBRID"
    private boolean requiresHumanApproval;
}
