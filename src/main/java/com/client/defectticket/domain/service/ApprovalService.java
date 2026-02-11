package com.client.defectticket.domain.service;

import com.client.defectticket.api.dto.ApprovalDecisionDto;
import com.client.defectticket.api.dto.ApprovalRequestDto;
import com.client.defectticket.domain.model.ApprovalRequest;
import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.enums.ApprovalStatus;
import com.client.defectticket.domain.repository.ApprovalRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.SendTaskFailureRequest;
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing HITL approval workflow.
 * Handles approval decisions and tracks AI vs Human divergence.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRequestRepository approvalRepository;
    private final SfnClient sfnClient;
    private final ObjectMapper objectMapper;

    @Value("${hitl.approval.track-divergence:true}")
    private boolean trackDivergence;

    @Value("${hitl.approval.skip-workflow-callback:false}")
    private boolean skipWorkflowCallback;

    /**
     * Find all pending approvals for HITL dashboard.
     */
    public List<ApprovalRequestDto> findPendingApprovals() {
        return approvalRepository.findByStatus(ApprovalStatus.PENDING)
                .stream()
                .map(ApprovalRequestDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Process approval decision from human reviewer.
     * Tracks divergence if enabled and resumes Step Functions workflow.
     */
    public void processDecision(ApprovalDecisionDto decision) {
        ApprovalRequest approval = approvalRepository.findById(decision.getApprovalId())
                .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + decision.getApprovalId()));

        log.info("Processing approval decision: {} by {}", 
                approval.getApprovalId(), decision.getReviewerEmail());

        // Track AI vs Human divergence
        if (trackDivergence) {
            boolean divergence = calculateDivergence(approval, decision);
            approval.setAiVsHumanDivergence(divergence);
            
            if (divergence) {
                log.warn("AI vs Human DIVERGENCE detected for approval {}: human {}",
                        approval.getApprovalId(), 
                        decision.getApproved() ? "APPROVED" : "REJECTED");
            }
        }

        // Update approval status
        approval.setStatus(decision.getApproved() ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        approval.setReviewerEmail(decision.getReviewerEmail());
        approval.setReviewerComments(decision.getComments());
        approval.setReviewedAt(Instant.now());

        approvalRepository.save(approval);

        // Resume Step Functions execution (skip in local mode for testing)
        if (!skipWorkflowCallback) {
            resumeStepFunctionsWorkflow(approval, decision.getApproved());
        } else {
            log.warn("Skipping Step Functions workflow callback (local testing mode)");
        }

        log.info("Approval {} processed: {} by {}", 
                approval.getApprovalId(), approval.getStatus(), decision.getReviewerEmail());
    }

    /**
     * Calculate if human decision diverges from AI recommendation.
     * Returns true if human overrides AI decision.
     */
    private boolean calculateDivergence(ApprovalRequest approval, ApprovalDecisionDto decision) {
        try {
            if (approval.getAiRecommendation() == null) {
                return false;  // No AI recommendation to compare
            }

            Classification aiClassification = objectMapper.readValue(
                approval.getAiRecommendation(), Classification.class);

            // If AI required approval and human rejected, that's divergence
            if (aiClassification.isRequiresHumanApproval() && !decision.getApproved()) {
                return true;
            }

            // If AI was confident (no approval needed) but human rejected, that's divergence
            if (!aiClassification.isRequiresHumanApproval() && !decision.getApproved()) {
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("Failed to calculate divergence for approval {}", approval.getApprovalId(), e);
            return false;
        }
    }

    /**
     * Resume Step Functions workflow via task token callback.
     */
    private void resumeStepFunctionsWorkflow(ApprovalRequest approval, boolean approved) {
        try {
            if (approved) {
                Map<String, String> output = new HashMap<>();
                output.put("decision", "APPROVED");
                output.put("reviewerEmail", approval.getReviewerEmail());
                
                SendTaskSuccessRequest request = SendTaskSuccessRequest.builder()
                        .taskToken(approval.getTaskToken())
                        .output(objectMapper.writeValueAsString(output))
                        .build();

                sfnClient.sendTaskSuccess(request);
                log.info("Step Functions execution resumed successfully for approval {}", approval.getApprovalId());

            } else {
                SendTaskFailureRequest request = SendTaskFailureRequest.builder()
                        .taskToken(approval.getTaskToken())
                        .error("ApprovalRejected")
                        .cause("Ticket classification rejected by reviewer: " + approval.getReviewerEmail())
                        .build();

                sfnClient.sendTaskFailure(request);
                log.info("Step Functions execution failed for approval {} (rejected)", approval.getApprovalId());
            }

        } catch (Exception e) {
            log.error("Failed to resume Step Functions for approval {}", approval.getApprovalId(), e);
            throw new RuntimeException("Failed to resume workflow", e);
        }
    }
}
