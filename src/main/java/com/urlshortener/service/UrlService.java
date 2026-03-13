package com.urlshortener.service;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.exception.ShortCodeAlreadyExistsException;
import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Generator;
import com.urlshortener.util.QRCodeGenerator;
import com.urlshortener.util.UrlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core business logic for URL shortening, resolution, analytics, and QR codes.
 *
 * <p>All public methods are @Transactional — Spring wraps them in a DB
 * transaction that commits on success and rolls back on any RuntimeException.
 *
 * <p>@RequiredArgsConstructor (Lombok) generates a constructor for all
 * final fields, enabling constructor-based dependency injection — the
 * recommended Spring approach over field @Autowired.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository   urlRepository;
    private final Base62Generator base62Generator;
    private final QRCodeGenerator qrCodeGenerator;
    private final UrlMapper       urlMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-code-length:6}")
    private int shortCodeLength;

    @Value("${app.qr.width:300}")
    private int qrWidth;

    @Value("${app.qr.height:300}")
    private int qrHeight;

    // ═══════════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Shortens a URL.
     *
     * <p>Strategy:
     * <ol>
     *   <li>If no customCode and URL already exists → return existing entry.</li>
     *   <li>If customCode provided → validate uniqueness, then use it.</li>
     *   <li>Otherwise → generate a unique random Base62 code.</li>
     * </ol>
     *
     * @param request Validated {@link ShortenRequest} DTO
     * @return {@link UrlResponse} with the short URL and metadata
     */
    @Transactional
    public UrlResponse shortenUrl(ShortenRequest request) {
        log.info("Shortening URL: {}", request.getUrl());

        String shortCode;

        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            // ── Custom short code requested ───────────────────────────────
            shortCode = request.getCustomCode().trim();
            if (urlRepository.existsByShortCode(shortCode)) {
                throw new ShortCodeAlreadyExistsException(shortCode);
            }
            log.info("Using custom short code: {}", shortCode);

        } else {
            // ── Check for existing entry (deduplication) ──────────────────
            Optional<UrlMapping> existing =
                urlRepository.findByOriginalUrlAndActiveTrue(request.getUrl());

            if (existing.isPresent()) {
                log.info("Returning existing short code: {}", existing.get().getShortCode());
                return urlMapper.toResponse(existing.get(), baseUrl);
            }

            // ── Generate a unique random Base62 code ──────────────────────
            shortCode = generateUniqueCode();
            log.info("Generated short code: {}", shortCode);
        }

        // ── Build and persist the entity ──────────────────────────────────
        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(request.getUrl())
                .shortCode(shortCode)
                .expiresAt(resolveExpiry(request.getExpiryDays()))
                .build();

        UrlMapping saved = urlRepository.save(mapping);
        log.info("Saved URL mapping: {} → {}", shortCode, request.getUrl());

        return urlMapper.toResponse(saved, baseUrl);
    }

    /**
     * Resolves a short code to its original URL and increments the click counter.
     * Validates that the mapping exists, is active, and has not expired.
     *
     * @param shortCode The short code from the URL path
     * @return The original long URL to redirect to
     */
    @Transactional
    public String resolveShortCode(String shortCode) {
        log.debug("Resolving short code: {}", shortCode);

        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        validateMapping(mapping, shortCode);

        // Atomically increment click counter (JPQL UPDATE, no entity reload needed)
        urlRepository.incrementClickCount(shortCode);

        log.info("Redirecting {} → {} (click #{})",
                shortCode, mapping.getOriginalUrl(), mapping.getClickCount() + 1);

        return mapping.getOriginalUrl();
    }

    /**
     * Retrieves full metadata for a short code without incrementing click count.
     *
     * @param shortCode The short code
     * @return {@link UrlResponse} with full metadata
     */
    @Transactional(readOnly = true)
    public UrlResponse getInfo(String shortCode) {
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        return urlMapper.toResponse(mapping, baseUrl);
    }

    /**
     * Generates a QR Code PNG for the short URL associated with a short code.
     *
     * @param shortCode The short code
     * @return PNG image as a byte array
     */
    @Transactional(readOnly = true)
    public byte[] generateQRCode(String shortCode) {
        // Validate the short code exists
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        String shortUrl = baseUrl + "/" + mapping.getShortCode();
        log.debug("Generating QR code for: {}", shortUrl);

        return qrCodeGenerator.generateQRCode(shortUrl, qrWidth, qrHeight);
    }

    /**
     * Soft-deletes a short URL by marking it inactive.
     * Subsequent redirect attempts will return 404.
     *
     * @param shortCode The short code to deactivate
     */
    @Transactional
    public void deactivateUrl(String shortCode) {
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        mapping.setActive(false);
        urlRepository.save(mapping);
        log.info("Deactivated short code: {}", shortCode);
    }

    /**
     * Returns all URL mappings (for admin/debug use).
     */
    @Transactional(readOnly = true)
    public List<UrlResponse> getAllUrls() {
        return urlRepository.findAll()
                .stream()
                .map(m -> urlMapper.toResponse(m, baseUrl))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Generates a unique Base62 short code using a collision-retry loop.
     * The probability of even one collision is ~1 in 10 billion at low volumes.
     */
    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = base62Generator.generate(shortCodeLength);
            attempts++;
            if (attempts > 10) {
                // Extend code length to escape a very saturated namespace
                code = base62Generator.generate(shortCodeLength + 2);
            }
        } while (urlRepository.existsByShortCode(code));

        return code;
    }

    /**
     * Validates that a URL mapping is active and not expired.
     * Throws domain-specific exceptions (never raw strings) so the
     * GlobalExceptionHandler can map them to the right HTTP status.
     */
    private void validateMapping(UrlMapping mapping, String shortCode) {
        if (!mapping.isActive()) {
            throw new ShortCodeNotFoundException(shortCode);
        }
        if (mapping.getExpiresAt() != null &&
                mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(shortCode, mapping.getExpiresAt());
        }
    }

    /**
     * Computes the expiry timestamp from an optional TTL in days.
     * Returns null if no expiry was requested.
     */
    private LocalDateTime resolveExpiry(Integer expiryDays) {
        if (expiryDays == null || expiryDays <= 0) {
            return null;
        }
        return LocalDateTime.now().plusDays(expiryDays);
    }
}
