package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp = LocalDateTime.now();
    private List<FieldError> fieldErrors;

    // ── Getters ──────────────────────────────────────────
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }

    // ── Setters ──────────────────────────────────────────
    public void setStatus(int status) { this.status = status; }
    public void setError(String error) { this.error = error; }
    public void setMessage(String message) { this.message = message; }
    public void setPath(String path) { this.path = path; }
    public void setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }

    // ── Builder ──────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ErrorResponse r = new ErrorResponse();
        public Builder status(int v) { r.status = v; return this; }
        public Builder error(String v) { r.error = v; return this; }
        public Builder message(String v) { r.message = v; return this; }
        public Builder path(String v) { r.path = v; return this; }
        public Builder fieldErrors(List<FieldError> v) { r.fieldErrors = v; return this; }
        public ErrorResponse build() { return r; }
    }

    // ── Inner Class ──────────────────────────────────────
    public static class FieldError {
        private String field;
        private String message;

        public FieldError() {}
        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        public String getField() { return field; }
        public String getMessage() { return message; }
    }
}
