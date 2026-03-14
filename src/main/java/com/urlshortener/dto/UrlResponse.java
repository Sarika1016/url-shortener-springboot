package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlResponse {

    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private String qrCodeUrl;
    private Long clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean active;

    // ── Constructors ─────────────────────────────────────
    public UrlResponse() {}

    // ── Getters ──────────────────────────────────────────
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public String getShortUrl() { return shortUrl; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public Long getClickCount() { return clickCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Boolean getActive() { return active; }

    // ── Setters ──────────────────────────────────────────
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setActive(Boolean active) { this.active = active; }

    // ── Builder ──────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final UrlResponse r = new UrlResponse();
        public Builder originalUrl(String v) { r.originalUrl = v; return this; }
        public Builder shortCode(String v) { r.shortCode = v; return this; }
        public Builder shortUrl(String v) { r.shortUrl = v; return this; }
        public Builder qrCodeUrl(String v) { r.qrCodeUrl = v; return this; }
        public Builder clickCount(Long v) { r.clickCount = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
        public Builder expiresAt(LocalDateTime v) { r.expiresAt = v; return this; }
        public Builder active(Boolean v) { r.active = v; return this; }
        public UrlResponse build() { return r; }
    }
}
