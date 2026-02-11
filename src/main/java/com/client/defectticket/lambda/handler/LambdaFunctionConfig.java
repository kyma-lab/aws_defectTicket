package com.client.defectticket.lambda.handler;

import com.client.defectticket.lambda.handler.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * Spring Cloud Function configuration for AWS Lambda.
 * Each function bean corresponds to a Lambda function.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class LambdaFunctionConfig {

    private final TicketIngestionHandler ingestionHandler;
    private final ClassificationHandler classificationHandler;
    private final ApprovalRequestHandler approvalRequestHandler;
    private final LoadBatchTicketsHandler loadBatchTicketsHandler;

    /**
     * Batch ingestion function.
     * Input: BatchIngestionRequest
     * Output: BatchIngestionResponse with ticket IDs
     */
    @Bean
    public Function<BatchIngestionRequest, BatchIngestionResponse> ingestBatch() {
        return request -> {
            log.info("Lambda function 'ingestBatch' invoked for batch: {}", request.getBatchId());
            return ingestionHandler.handle(request);
        };
    }

    /**
     * Classification function.
     * Input: ClassificationRequest (only ticketId)
     * Output: ClassificationResponse with classification result
     */
    @Bean
    public Function<ClassificationRequest, ClassificationResponse> classifyTicket() {
        return request -> {
            log.info("Lambda function 'classifyTicket' invoked for ticket: {}", request.getTicketId());
            return classificationHandler.handle(request);
        };
    }

    /**
     * Approval request creation function.
     * Input: ApprovalCreationRequest with task token
     * Output: ApprovalCreationResponse
     */
    @Bean
    public Function<ApprovalCreationRequest, ApprovalCreationResponse> createApprovalRequest() {
        return request -> {
            log.info("Lambda function 'createApprovalRequest' invoked for ticket: {}", request.getTicketId());
            return approvalRequestHandler.handle(request);
        };
    }

    /**
     * Load batch tickets function (Claim Check Pattern).
     * Input: Map with batchId
     * Output: List of ticket IDs
     */
    @Bean
    public Function<java.util.Map<String, String>, java.util.List<String>> loadBatchTickets() {
        return input -> {
            log.info("Lambda function 'loadBatchTickets' invoked for batch: {}", input.get("batchId"));
            return loadBatchTicketsHandler.handle(input);
        };
    }
}
