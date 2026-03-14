package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public class ShortenRequest {

    @NotBlank(message = "URL must not be blank")
    @URL(message = "Must be a valid URL (include http:// or https://)")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String url;

    private String customCode;
    private Integer expiryDays;

    // ── Constructors ─────────────────────────────────────
    public ShortenRequest() {}

    public ShortenRequest(String url, String customCode, Integer expiryDays) {
        this.url = url;
        this.customCode = customCode;
        this.expiryDays = expiryDays;
    }

    // ── Getters ──────────────────────────────────────────
    public String getUrl() { return url; }
    public String getCustomCode() { return customCode; }
    public Integer getExpiryDays() { return expiryDays; }

    // ── Setters ──────────────────────────────────────────
    public void setUrl(String url) { this.url = url; }
    public void setCustomCode(String customCode) { this.customCode = customCode; }
    public void setExpiryDays(Integer expiryDays) { this.expiryDays = expiryDays; }
}
