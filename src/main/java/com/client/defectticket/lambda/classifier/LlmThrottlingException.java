package com.client.defectticket.lambda.classifier;

/**
 * Exception thrown when Bedrock throttles requests.
 * Step Functions should retry with exponential backoff.
 */
public class LlmThrottlingException extends RuntimeException {
    
    public LlmThrottlingException(String message) {
        super(message);
    }
    
    public LlmThrottlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
