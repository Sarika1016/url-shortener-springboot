package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO returned for the POST /api/urls/shorten response
 * and the GET /api/urls/{shortCode}/info response.
 *
 * @JsonInclude(NON_NULL) omits null fields from the JSON output,
 * keeping responses clean (e.g., expiresAt not shown if not set).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlResponse {

    /** The original long URL. */
    private String originalUrl;

    /** The generated or custom short code (e.g., "aB3xZ9"). */
    private String shortCode;

    /** The full short URL ready to share (e.g., "http://localhost:8080/aB3xZ9"). */
    private String shortUrl;

    /** URL to retrieve the QR code PNG image. */
    private String qrCodeUrl;

    /** Total number of times this short URL has been accessed. */
    private Long clickCount;

    /** Timestamp when this mapping was created. */
    private LocalDateTime createdAt;

    /** Timestamp when this short URL expires (null = never). */
    private LocalDateTime expiresAt;

    /** Whether this short URL is currently active. */
    private Boolean active;
}
