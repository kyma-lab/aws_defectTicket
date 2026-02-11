package com.client.defectticket.messaging.sqs;

import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.TicketStatus;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import com.client.defectticket.lambda.handler.dto.BatchIngestionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles SQS messages for batch ingestion.
 * Provides buffering and resilience before Step Functions execution.
 * 
 * Key benefits:
 * - Load spike buffering
 * - Automatic retry with DLQ
 * - Decoupling from REST API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchIngestionMessageHandler {

    private final SqsClient sqsClient;
    private final SfnClient sfnClient;
    private final DefectTicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.ingestion-queue-url}")
    private String queueUrl;

    @Value("${aws.stepfunctions.state-machine-arn}")
    private String stateMachineArn;

    @Value("${aws.sqs.polling-enabled:false}")
    private boolean pollingEnabled;

    @Value("${aws.stepfunctions.skip-execution:false}")
    private boolean skipStepFunctionsExecution;

    /**
     * Poll SQS queue and start Step Functions for each batch.
     * Scheduled to run every 10 seconds when polling is enabled.
     * Stores tickets in DynamoDB first (Claim Check Pattern), then starts Step Functions.
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void pollAndProcess() {
        if (!pollingEnabled) {
            return;
        }
        
        log.debug("Polling SQS queue: {}", queueUrl);

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)  // Long polling for efficiency
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
        log.info("Received {} messages from SQS", messages.size());

        for (Message message : messages) {
            try {
                processMessage(message);
                deleteMessage(message);
            } catch (Exception e) {
                log.error("Failed to process SQS message: {}", message.messageId(), e);
                // Message will be retried or moved to DLQ after max attempts
            }
        }
    }

    private void processMessage(Message message) throws Exception {
        BatchIngestionRequest request = objectMapper.readValue(
            message.body(), BatchIngestionRequest.class);

        log.info("Processing batch ingestion for batchId: {}, tickets: {}", 
                request.getBatchId(), request.getTickets().size());

        // Store tickets in DynamoDB (Claim Check Pattern)
        int storedCount = 0;
        for (BatchIngestionRequest.TicketInput ticketInput : request.getTickets()) {
            DefectTicket ticket = new DefectTicket();
            ticket.setTicketId(UUID.randomUUID().toString());
            ticket.setBatchId(request.getBatchId());
            ticket.setSourceSystem(request.getSourceSystem());
            ticket.setSourceReference(ticketInput.getSourceReference());
            ticket.setTitle(ticketInput.getTitle());
            ticket.setDescription(ticketInput.getDescription());
            ticket.setStatus(TicketStatus.NEW);
            ticket.setCreatedAt(Instant.now());
            ticket.setUpdatedAt(Instant.now());
            
            ticketRepository.save(ticket);
            storedCount++;
            log.debug("Stored ticket: {} from {}", ticket.getTicketId(), ticketInput.getSourceReference());
        }
        
        log.info("Stored {} tickets in DynamoDB for batch: {}", storedCount, request.getBatchId());

        // Start Step Functions workflow (skip in local mode if not configured)
        if (!skipStepFunctionsExecution) {
            // Claim Check Pattern: Only pass batchId to Step Functions
            // Full ticket data is now in DynamoDB
            Map<String, String> input = new HashMap<>();
            input.put("batchId", request.getBatchId());
            input.put("sourceSystem", request.getSourceSystem());
            
            String executionInput = objectMapper.writeValueAsString(input);

            StartExecutionRequest executionRequest = StartExecutionRequest.builder()
                    .stateMachineArn(stateMachineArn)
                    .name(request.getBatchId() + "-" + UUID.randomUUID())
                    .input(executionInput)
                    .build();

            var response = sfnClient.startExecution(executionRequest);
            
            log.info("Started Step Functions execution {} for batch: {}",
                    response.executionArn(), request.getBatchId());
        } else {
            log.warn("Skipping Step Functions execution (local testing mode) for batch: {}", request.getBatchId());
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        
        sqsClient.deleteMessage(deleteRequest);
        log.debug("Deleted message from SQS: {}", message.messageId());
    }
}
