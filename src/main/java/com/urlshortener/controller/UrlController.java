package com.urlshortener.controller;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST Controller exposing all URL Shortener endpoints.
 *
 * <p>Two route groups:
 * <ul>
 *   <li><strong>/api/urls/**</strong> — JSON API endpoints (shorten, info, QR, list, deactivate)</li>
 *   <li><strong>/{shortCode}</strong> — The public redirect endpoint (root level, browser-friendly)</li>
 * </ul>
 *
 * <p>The controller itself contains NO business logic — it only validates input,
 * delegates to {@link UrlService}, and shapes the HTTP response.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final UrlService urlService;

    // ═══════════════════════════════════════════════════════════════════════════
    // API ENDPOINTS  (/api/urls/*)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/urls/shorten
     *
     * <p>Accepts a long URL and returns a shortened URL with metadata.
     * {@code @Valid} triggers Jakarta Bean Validation on {@link ShortenRequest}.
     * If validation fails, {@link com.urlshortener.exception.GlobalExceptionHandler}
     * intercepts and returns a structured 400 error.
     *
     * <p>Example request:
     * <pre>
     * POST /api/urls/shorten
     * Content-Type: application/json
     *
     * {
     *   "url": "https://www.example.com/very/long/path?query=value",
     *   "customCode": "mylink",
     *   "expiryDays": 30
     * }
     * </pre>
     */
    @PostMapping("/api/urls/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(
            @Valid @RequestBody ShortenRequest request) {

        log.info("POST /api/urls/shorten — {}", request.getUrl());

        UrlResponse response = urlService.shortenUrl(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)     // 201 Created
                .body(response);
    }

    /**
     * GET /api/urls/{shortCode}/info
     *
     * <p>Returns metadata for a short code — no redirect, no click increment.
     * Useful for dashboards and link previews.
     */
    @GetMapping("/api/urls/{shortCode}/info")
    public ResponseEntity<UrlResponse> getInfo(
            @PathVariable String shortCode) {

        log.info("GET /api/urls/{}/info", shortCode);

        return ResponseEntity.ok(urlService.getInfo(shortCode));
    }

    /**
     * GET /api/urls/{shortCode}/qr
     *
     * <p>Streams a 300×300 PNG QR Code image for the given short URL.
     * Content-Type is set to image/png so browsers render it inline.
     */
    @GetMapping(value = "/api/urls/{shortCode}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCode(
            @PathVariable String shortCode) {

        log.info("GET /api/urls/{}/qr", shortCode);

        byte[] qrBytes = urlService.generateQRCode(shortCode);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"qr-" + shortCode + ".png\"")
                .body(qrBytes);
    }

    /**
     * GET /api/urls
     *
     * <p>Returns all URL mappings. In a real production system this would
     * be secured, paginated, and filtered by owner/tenant.
     */
    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        log.info("GET /api/urls");
        return ResponseEntity.ok(urlService.getAllUrls());
    }

    /**
     * DELETE /api/urls/{shortCode}
     *
     * <p>Soft-deletes (deactivates) a short URL. The record is retained in
     * the database for analytics — it simply stops redirecting (returns 404).
     */
    @DeleteMapping("/api/urls/{shortCode}")
    public ResponseEntity<Void> deactivate(
            @PathVariable String shortCode) {

        log.info("DELETE /api/urls/{}", shortCode);

        urlService.deactivateUrl(shortCode);

        return ResponseEntity.noContent().build();   // 204 No Content
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUBLIC REDIRECT  (/{shortCode})
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /{shortCode}
     *
     * <p>The core redirect endpoint. When a browser hits this URL, it
     * receives an HTTP 302 Found response whose Location header points
     * to the original URL — the browser then follows it automatically.
     *
     * <p>Also increments the click counter for analytics.
     *
     * <p>Example:
     * <pre>
     * GET http://localhost:8080/aB3xZ9
     * → HTTP 302 Found
     * → Location: https://www.example.com/very/long/path
     * </pre>
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode) {

        log.info("GET /{} — redirecting", shortCode);

        String originalUrl = urlService.resolveShortCode(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)                // 302 Found
                .location(URI.create(originalUrl))
                .build();
    }
}
