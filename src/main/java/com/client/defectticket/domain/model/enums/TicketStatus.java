package com.client.defectticket.domain.model.enums;

/**
 * Lifecycle status of a defect ticket.
 */
public enum TicketStatus {
    NEW,
    VALIDATED,
    CLASSIFIED,
    PENDING_CLASSIFICATION_APPROVAL,
    CLASSIFICATION_APPROVED,
    CLASSIFICATION_REJECTED,
    ASSIGNED,
    IN_PROGRESS,
    PENDING_FINAL_APPROVAL,
    RESOLVED,
    CLOSED,
    ARCHIVED
}
