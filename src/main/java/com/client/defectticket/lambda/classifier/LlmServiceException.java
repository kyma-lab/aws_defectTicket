package com.client.defectticket.lambda.classifier;

/**
 * Exception thrown when LLM classification fails.
 * Used for granular error handling in Step Functions.
 */
public class LlmServiceException extends RuntimeException {
    
    public LlmServiceException(String message) {
        super(message);
    }
    
    public LlmServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
