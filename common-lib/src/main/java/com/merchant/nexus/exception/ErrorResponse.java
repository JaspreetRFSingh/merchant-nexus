package com.merchant.nexus.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

/**
 * Standardized error response for API errors.
 * Demonstrates: Consistent error handling, API design best practices
 */
@Data
@Builder
public class ErrorResponse {
    private String errorCode;
    private String message;
    private List<FieldError> fieldErrors;
    private String path;
    @JsonProperty("timestamp")
    private Instant timestamp;
    private String requestId;

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    public static ErrorResponse of(String errorCode, String message) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse of(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }
}
