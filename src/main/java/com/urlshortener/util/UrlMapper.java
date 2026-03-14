package com.urlshortener.util;

import com.urlshortener.dto.UrlResponse;
import com.urlshortener.model.UrlMapping;
import org.springframework.stereotype.Component;

@Component
public class UrlMapper {

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
