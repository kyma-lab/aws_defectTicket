package com.client.defectticket.api.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests proper HTTP status code mapping and error response structure.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturn404ForIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Approval not found: test-id");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Approval not found: test-id");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void shouldReturn400ForValidationException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        
        FieldError fieldError1 = new FieldError("approvalDecisionDto", "approvalId", "must not be blank");
        FieldError fieldError2 = new FieldError("approvalDecisionDto", "reviewerEmail", "must be a valid email");
        
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getValidationErrors()).hasSize(2);
        assertThat(response.getBody().getValidationErrors()).containsEntry("approvalId", "must not be blank");
        assertThat(response.getBody().getValidationErrors()).containsEntry("reviewerEmail", "must be a valid email");
    }

    @Test
    void shouldReturn502ForWorkflowException() {
        // Given
        RuntimeException cause = new RuntimeException("Connection timeout");
        RuntimeException exception = new RuntimeException("Failed to resume workflow", cause);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRuntimeException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(502);
        assertThat(response.getBody().getError()).isEqualTo("Bad Gateway");
        assertThat(response.getBody().getMessage()).contains("Failed to communicate with workflow service");
        assertThat(response.getBody().getMessage()).contains("Connection timeout");
    }

    @Test
    void shouldReturn500ForGenericRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRuntimeException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void shouldHandleNullPointerException() {
        // Given
        RuntimeException exception = new NullPointerException("Null value encountered");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRuntimeException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void shouldNotIncludeValidationErrorsForNonValidationException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).isNull();
    }
}
