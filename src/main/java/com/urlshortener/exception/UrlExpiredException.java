package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

/**
 * Thrown when a short URL is accessed after its expiry date.
 * Maps to HTTP 410 Gone — the resource existed but is no longer available.
 */
@ResponseStatus(HttpStatus.GONE)
public class UrlExpiredException extends RuntimeException {

    private final String shortCode;
    private final LocalDateTime expiredAt;

    public UrlExpiredException(String shortCode, LocalDateTime expiredAt) {
        super("Short URL '" + shortCode + "' expired at " + expiredAt);
        this.shortCode = shortCode;
        this.expiredAt = expiredAt;
    }

    public String getShortCode() { return shortCode; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
}
