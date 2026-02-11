package com.client.defectticket.lambda.classifier;

import com.client.defectticket.domain.model.Classification;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.bedrock.anthropic.BedrockAnthropicChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.model.ThrottlingException;

/**
 * LLM classification service using Spring AI with AWS Bedrock.
 * Uses BeanOutputConverter for structured JSON output (no manual parsing).
 *
 * Only active in AWS/production profiles. Use MockClassificationService for local development.
 */
@Slf4j
@Service
@Profile("!local & !test")
@RequiredArgsConstructor
public class SpringAiClassificationService {

    private final BedrockAnthropicChatModel chatModel;

    @Value("${llm.confidence-threshold}")
    private double confidenceThreshold;

    /**
     * Classify a defect ticket using AI.
     * 
     * @param ticket The ticket to classify
     * @return Classification result with confidence score
     * @throws LlmThrottlingException if Bedrock throttles the request
     * @throws LlmServiceException if classification fails
     */
    public Classification classify(DefectTicket ticket) {
        try {
            log.info("Classifying ticket {} with Spring AI Bedrock", ticket.getTicketId());

            BeanOutputConverter<ClassificationOutput> outputConverter = 
                new BeanOutputConverter<>(ClassificationOutput.class);

            String prompt = buildClassificationPrompt(ticket, outputConverter.getFormat());

            ChatResponse response = chatModel.call(new Prompt(prompt));
            
            ClassificationOutput output = outputConverter.convert(
                response.getResult().getOutput().getContent());

            Classification classification = Classification.builder()
                    .category(output.category())
                    .subcategory(output.subcategory())
                    .severity(Severity.valueOf(output.severity()))
                    .priority(output.priority())
                    .confidenceScore(output.confidenceScore())
                    .reasoning(output.reasoning())
                    .classificationSource("LLM")
                    .requiresHumanApproval(output.confidenceScore() < confidenceThreshold)
                    .build();

            log.info("Classification complete for ticket {}: category={}, confidence={}", 
                    ticket.getTicketId(), classification.getCategory(), 
                    classification.getConfidenceScore());

            return classification;

        } catch (ThrottlingException e) {
            log.warn("Bedrock throttling for ticket {}", ticket.getTicketId(), e);
            throw new LlmThrottlingException("Bedrock request throttled", e);
        } catch (Exception e) {
            log.error("LLM classification failed for ticket {}", ticket.getTicketId(), e);
            throw new LlmServiceException("Failed to classify ticket", e);
        }
    }

    private String buildClassificationPrompt(DefectTicket ticket, String format) {
        return String.format("""
                You are an expert defect ticket classifier for a software development team.
                
                Analyze the following ticket and provide a structured classification:
                
                1. **Category**: Primary classification (Bug, Enhancement, Security, Performance, Documentation, etc.)
                2. **Subcategory**: More specific classification within the category
                3. **Severity**: Impact level (CRITICAL, HIGH, MEDIUM, LOW, TRIVIAL)
                   - CRITICAL: System down, data loss, security breach
                   - HIGH: Major functionality broken, significant performance degradation
                   - MEDIUM: Feature not working as expected, moderate impact
                   - LOW: Minor issues, cosmetic problems
                   - TRIVIAL: Typos, very minor improvements
                4. **Priority**: Urgency (1=Highest to 5=Lowest)
                5. **Confidence Score**: Your confidence in this classification (0.0 to 1.0)
                6. **Reasoning**: Brief explanation of your classification
                
                **Ticket Information:**
                - Title: %s
                - Description: %s
                - Source System: %s
                
                %s
                """, 
                ticket.getTitle(), 
                ticket.getDescription(), 
                ticket.getSourceSystem(),
                format);
    }

    /**
     * Output structure for BeanOutputConverter.
     * Must be a record or simple POJO for Jackson serialization.
     */
    public record ClassificationOutput(
        @JsonProperty("category") String category,
        @JsonProperty("subcategory") String subcategory,
        @JsonProperty("severity") String severity,
        @JsonProperty("priority") Integer priority,
        @JsonProperty("confidenceScore") Double confidenceScore,
        @JsonProperty("reasoning") String reasoning
    ) {}
}
