package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardised error response body returned for all API errors.
 * Provides a consistent shape so clients can reliably parse failures.
 *
 * Example JSON:
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Short code 'abc123' not found",
 *   "path": "/abc123",
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** HTTP status code (e.g., 400, 404, 500). */
    private int status;

    /** Short HTTP status description (e.g., "Bad Request"). */
    private String error;

    /** Human-readable error message. */
    private String message;

    /** The request path that triggered the error. */
    private String path;

    /** ISO-8601 timestamp of when the error occurred. */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * List of field-level validation errors.
     * Only populated for 400 validation failures.
     */
    private List<FieldError> fieldErrors;

    /** Inner class for per-field validation errors. */
    @Data
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}
