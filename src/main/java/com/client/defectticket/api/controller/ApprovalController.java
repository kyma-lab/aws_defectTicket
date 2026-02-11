package com.client.defectticket.api.controller;

import com.client.defectticket.api.dto.ApprovalDecisionDto;
import com.client.defectticket.api.dto.ApprovalRequestDto;
import com.client.defectticket.domain.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for HITL approval workflow.
 * Provides endpoints for dashboard to list and process approvals.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    /**
     * List all pending approval requests.
     * Used by HITL dashboard to show items requiring human review.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalRequestDto>> listPendingApprovals() {
        log.info("API: Fetching pending approvals");
        List<ApprovalRequestDto> approvals = approvalService.findPendingApprovals();
        log.info("API: Returning {} pending approvals", approvals.size());
        return ResponseEntity.ok(approvals);
    }

    /**
     * Submit approval decision.
     * Triggers Step Functions workflow resumption and tracks divergence.
     */
    @PostMapping("/decide")
    public ResponseEntity<Void> submitDecision(@Valid @RequestBody ApprovalDecisionDto decision) {
        log.info("API: Processing approval decision for: {} (approved: {})", 
                decision.getApprovalId(), decision.getApproved());
        
        approvalService.processDecision(decision);
        
        return ResponseEntity.ok().build();
    }
}
