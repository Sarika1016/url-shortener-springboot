package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested short code does not exist in the database.
 * Maps to HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShortCodeNotFoundException extends RuntimeException {

    private final String shortCode;

    public ShortCodeNotFoundException(String shortCode) {
        super("Short code '" + shortCode + "' not found");
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}
