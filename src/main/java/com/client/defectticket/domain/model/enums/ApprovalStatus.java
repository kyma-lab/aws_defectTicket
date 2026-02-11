package com.client.defectticket.domain.model.enums;

/**
 * Status of a HITL approval request.
 */
public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    TIMED_OUT,
    ESCALATED
}
