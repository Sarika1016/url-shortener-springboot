package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * DTO for the POST /api/urls/shorten request body.
 * Validated by Jakarta Bean Validation before reaching the service layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenRequest {

    /**
     * The long URL to shorten.
     * Must be a valid HTTP/HTTPS URL, max 2048 characters.
     */
    @NotBlank(message = "URL must not be blank")
    @URL(message = "Must be a valid URL (include http:// or https://)")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String url;

    /**
     * Optional custom short code requested by the user.
     * If null, a random Base62 code is generated.
     * Only alphanumeric characters allowed, 3–20 chars.
     */
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]{3,20}$",
        message = "Custom code must be 3–20 alphanumeric characters (-, _ allowed)"
    )
    private String customCode;

    /**
     * Optional TTL in days. If provided, the short URL will expire
     * after this many days. Null means no expiry.
     */
    private Integer expiryDays;
}
