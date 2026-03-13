package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user requests a custom short code that is already taken.
 * Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ShortCodeAlreadyExistsException extends RuntimeException {

    private final String shortCode;

    public ShortCodeAlreadyExistsException(String shortCode) {
        super("Custom short code '" + shortCode + "' is already taken");
        this.shortCode = shortCode;
    }

    public String getShortCode() {
        return shortCode;
    }
}
