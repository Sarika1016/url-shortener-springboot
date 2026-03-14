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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Generator base62Generator;
    private final QRCodeGenerator qrCodeGenerator;
    private final UrlMapper urlMapper;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-code-length:6}")
    private int shortCodeLength;

    @Value("${app.qr.width:300}")
    private int qrWidth;

    @Value("${app.qr.height:300}")
    private int qrHeight;

    public UrlService(UrlRepository urlRepository,
                      Base62Generator base62Generator,
                      QRCodeGenerator qrCodeGenerator,
                      UrlMapper urlMapper) {
        this.urlRepository = urlRepository;
        this.base62Generator = base62Generator;
        this.qrCodeGenerator = qrCodeGenerator;
        this.urlMapper = urlMapper;
    }

    @Transactional
    public UrlResponse shortenUrl(ShortenRequest request) {
        String shortCode;

        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            shortCode = request.getCustomCode().trim();
            if (urlRepository.existsByShortCode(shortCode)) {
                throw new ShortCodeAlreadyExistsException(shortCode);
            }
        } else {
            Optional<UrlMapping> existing =
                urlRepository.findByOriginalUrlAndActiveTrue(request.getUrl());
            if (existing.isPresent()) {
                return urlMapper.toResponse(existing.get(), baseUrl);
            }
            shortCode = generateUniqueCode();
        }

        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(request.getUrl())
                .shortCode(shortCode)
                .expiresAt(resolveExpiry(request.getExpiryDays()))
                .build();

        UrlMapping saved = urlRepository.save(mapping);
        return urlMapper.toResponse(saved, baseUrl);
    }

    @Transactional
    public String resolveShortCode(String shortCode) {
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));

        validateMapping(mapping, shortCode);
        urlRepository.incrementClickCount(shortCode);
        return mapping.getOriginalUrl();
    }

    @Transactional(readOnly = true)
    public UrlResponse getInfo(String shortCode) {
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
        return urlMapper.toResponse(mapping, baseUrl);
    }

    @Transactional(readOnly = true)
    public byte[] generateQRCode(String shortCode) {
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
        String shortUrl = baseUrl + "/" + mapping.getShortCode();
        return qrCodeGenerator.generateQRCode(shortUrl, qrWidth, qrHeight);
    }

    @Transactional
    public void deactivateUrl(String shortCode) {
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortCodeNotFoundException(shortCode));
        mapping.setActive(false);
        urlRepository.save(mapping);
    }

    @Transactional(readOnly = true)
    public List<UrlResponse> getAllUrls() {
        return urlRepository.findAll()
                .stream()
                .map(m -> urlMapper.toResponse(m, baseUrl))
                .collect(Collectors.toList());
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = base62Generator.generate(shortCodeLength);
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    private void validateMapping(UrlMapping mapping, String shortCode) {
        if (!mapping.isActive()) {
            throw new ShortCodeNotFoundException(shortCode);
        }
        if (mapping.getExpiresAt() != null &&
                mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(shortCode, mapping.getExpiresAt());
        }
    }

    private LocalDateTime resolveExpiry(Integer expiryDays) {
        if (expiryDays == null || expiryDays <= 0) return null;
        return LocalDateTime.now().plusDays(expiryDays);
    }
}
