package com.client.defectticket.domain.model.enums;

/**
 * HITL approval gates in the workflow.
 */
public enum ApprovalGate {
    CLASSIFICATION_REVIEW,  // Gate 1: Review AI classification
    FINAL_APPROVAL          // Gate 2: Final approval before closing
}
