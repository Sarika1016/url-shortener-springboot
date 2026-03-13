package com.urlshortener.util;

import com.urlshortener.dto.UrlResponse;
import com.urlshortener.model.UrlMapping;
import org.springframework.stereotype.Component;

/**
 * Converts {@link UrlMapping} entities into {@link UrlResponse} DTOs.
 *
 * <p>Keeping mapping logic in a dedicated class (rather than in the service
 * or entity) follows the Single Responsibility Principle and makes it easy
 * to swap out a mapping framework (e.g., MapStruct) later.
 */
@Component
public class UrlMapper {

    /**
     * Maps a persisted UrlMapping entity to a UrlResponse DTO.
     *
     * @param mapping  The entity retrieved from the database
     * @param baseUrl  The application's base URL (from application.properties)
     * @return A fully populated UrlResponse DTO
     */
    public UrlResponse toResponse(UrlMapping mapping, String baseUrl) {
        return UrlResponse.builder()
                .originalUrl(mapping.getOriginalUrl())
                .shortCode(mapping.getShortCode())
                .shortUrl(baseUrl + "/" + mapping.getShortCode())
                .qrCodeUrl(baseUrl + "/api/urls/" + mapping.getShortCode() + "/qr")
                .clickCount(mapping.getClickCount())
                .createdAt(mapping.getCreatedAt())
                .expiresAt(mapping.getExpiresAt())
                .active(mapping.isActive())
                .build();
    }
}
