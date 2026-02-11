package com.client.defectticket.api.dto;

import com.client.defectticket.domain.model.ApprovalRequest;
import com.client.defectticket.domain.model.enums.ApprovalGate;
import com.client.defectticket.domain.model.enums.ApprovalStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * DTO for approval request display in HITL dashboard.
 */
@Value
@Builder
public class ApprovalRequestDto {
    String approvalId;
    String ticketId;
    ApprovalGate gate;
    ApprovalStatus status;
    String context;
    Instant createdAt;
    Instant expiresAt;
    String aiRecommendation;

    public static ApprovalRequestDto from(ApprovalRequest approval) {
        return ApprovalRequestDto.builder()
                .approvalId(approval.getApprovalId())
                .ticketId(approval.getTicketId())
                .gate(approval.getGate())
                .status(approval.getStatus())
                .context(approval.getContext())
                .createdAt(approval.getCreatedAt())
                .expiresAt(approval.getExpiresAt())
                .aiRecommendation(approval.getAiRecommendation())
                .build();
    }
}
