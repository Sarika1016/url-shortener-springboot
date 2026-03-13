package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when QR code generation fails due to an internal error.
 * Maps to HTTP 500 Internal Server Error.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class QRCodeGenerationException extends RuntimeException {

    public QRCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
