package com.client.defectticket.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for submitting approval decisions from HITL dashboard.
 */
@Data
public class ApprovalDecisionDto {
    
    @NotBlank
    private String approvalId;

    @NotNull
    private Boolean approved;

    @NotBlank
    @Email
    private String reviewerEmail;

    private String comments;
}
