package com.urlshortener;

import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.exception.ShortCodeAlreadyExistsException;
import com.urlshortener.exception.ShortCodeNotFoundException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.service.UrlService;
import com.urlshortener.util.Base62Generator;
import com.urlshortener.util.QRCodeGenerator;
import com.urlshortener.util.UrlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UrlService}.
 *
 * Uses Mockito to isolate the service from the database and utilities.
 * All dependencies are mocked — no Spring context is loaded.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UrlService Unit Tests")
class UrlServiceTest {

    @Mock private UrlRepository   urlRepository;
    @Mock private Base62Generator base62Generator;
    @Mock private QRCodeGenerator qrCodeGenerator;
    @Mock private UrlMapper       urlMapper;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "baseUrl",        "http://localhost:8080");
        ReflectionTestUtils.setField(urlService, "shortCodeLength", 6);
        ReflectionTestUtils.setField(urlService, "qrWidth",        300);
        ReflectionTestUtils.setField(urlService, "qrHeight",       300);
    }

    // ── shortenUrl ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("shortenUrl: new URL generates random code and saves mapping")
    void shortenUrl_newUrl_generatesCodeAndSaves() {
        ShortenRequest request = new ShortenRequest("https://example.com", null, null);
        UrlMapping saved = buildMapping("aB3xZ9", "https://example.com");
        UrlResponse expected = buildResponse("aB3xZ9");

        when(urlRepository.findByOriginalUrlAndActiveTrue("https://example.com"))
            .thenReturn(Optional.empty());
        when(base62Generator.generate(6)).thenReturn("aB3xZ9");
        when(urlRepository.existsByShortCode("aB3xZ9")).thenReturn(false);
        when(urlRepository.save(any())).thenReturn(saved);
        when(urlMapper.toResponse(saved, "http://localhost:8080")).thenReturn(expected);

        UrlResponse result = urlService.shortenUrl(request);

        assertThat(result).isEqualTo(expected);
        verify(urlRepository).save(any(UrlMapping.class));
    }

    @Test
    @DisplayName("shortenUrl: existing URL returns cached mapping without inserting")
    void shortenUrl_existingUrl_returnsCachedMapping() {
        ShortenRequest request = new ShortenRequest("https://example.com", null, null);
        UrlMapping existing = buildMapping("aB3xZ9", "https://example.com");
        UrlResponse expected = buildResponse("aB3xZ9");

        when(urlRepository.findByOriginalUrlAndActiveTrue("https://example.com"))
            .thenReturn(Optional.of(existing));
        when(urlMapper.toResponse(existing, "http://localhost:8080")).thenReturn(expected);

        UrlResponse result = urlService.shortenUrl(request);

        assertThat(result).isEqualTo(expected);
        verify(urlRepository, never()).save(any());
    }

    @Test
    @DisplayName("shortenUrl: custom code that is available gets used")
    void shortenUrl_customCode_available_usesIt() {
        ShortenRequest request = new ShortenRequest("https://example.com", "mylink", null);
        UrlMapping saved = buildMapping("mylink", "https://example.com");
        UrlResponse expected = buildResponse("mylink");

        when(urlRepository.existsByShortCode("mylink")).thenReturn(false);
        when(urlRepository.save(any())).thenReturn(saved);
        when(urlMapper.toResponse(saved, "http://localhost:8080")).thenReturn(expected);

        UrlResponse result = urlService.shortenUrl(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("shortenUrl: custom code already taken throws ShortCodeAlreadyExistsException")
    void shortenUrl_customCode_alreadyTaken_throwsConflict() {
        ShortenRequest request = new ShortenRequest("https://example.com", "taken", null);
        when(urlRepository.existsByShortCode("taken")).thenReturn(true);

        assertThatThrownBy(() -> urlService.shortenUrl(request))
            .isInstanceOf(ShortCodeAlreadyExistsException.class)
            .hasMessageContaining("taken");
    }

    // ── resolveShortCode ──────────────────────────────────────────────────────

    @Test
    @DisplayName("resolveShortCode: valid code returns original URL and increments counter")
    void resolveShortCode_valid_returnsUrlAndIncrements() {
        UrlMapping mapping = buildMapping("aB3xZ9", "https://example.com");
        when(urlRepository.findByShortCode("aB3xZ9")).thenReturn(Optional.of(mapping));

        String result = urlService.resolveShortCode("aB3xZ9");

        assertThat(result).isEqualTo("https://example.com");
        verify(urlRepository).incrementClickCount("aB3xZ9");
    }

    @Test
    @DisplayName("resolveShortCode: unknown code throws ShortCodeNotFoundException")
    void resolveShortCode_unknown_throwsNotFound() {
        when(urlRepository.findByShortCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.resolveShortCode("unknown"))
            .isInstanceOf(ShortCodeNotFoundException.class);
    }

    @Test
    @DisplayName("resolveShortCode: expired URL throws UrlExpiredException")
    void resolveShortCode_expired_throwsGone() {
        UrlMapping mapping = buildMapping("aB3xZ9", "https://example.com");
        mapping.setExpiresAt(LocalDateTime.now().minusDays(1)); // expired yesterday
        when(urlRepository.findByShortCode("aB3xZ9")).thenReturn(Optional.of(mapping));

        assertThatThrownBy(() -> urlService.resolveShortCode("aB3xZ9"))
            .isInstanceOf(UrlExpiredException.class);
    }

    @Test
    @DisplayName("resolveShortCode: inactive URL throws ShortCodeNotFoundException")
    void resolveShortCode_inactive_throwsNotFound() {
        UrlMapping mapping = buildMapping("aB3xZ9", "https://example.com");
        mapping.setActive(false);
        when(urlRepository.findByShortCode("aB3xZ9")).thenReturn(Optional.of(mapping));

        assertThatThrownBy(() -> urlService.resolveShortCode("aB3xZ9"))
            .isInstanceOf(ShortCodeNotFoundException.class);
    }

    // ── generateQRCode ────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateQRCode: returns byte array from QRCodeGenerator")
    void generateQRCode_returnsBytes() {
        UrlMapping mapping = buildMapping("aB3xZ9", "https://example.com");
        byte[] expected = new byte[]{1, 2, 3};

        when(urlRepository.findByShortCode("aB3xZ9")).thenReturn(Optional.of(mapping));
        when(qrCodeGenerator.generateQRCode(anyString(), eq(300), eq(300))).thenReturn(expected);

        byte[] result = urlService.generateQRCode("aB3xZ9");

        assertThat(result).isEqualTo(expected);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UrlMapping buildMapping(String shortCode, String originalUrl) {
        return UrlMapping.builder()
                .id(1L)
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .active(true)
                .clickCount(0L)
                .build();
    }

    private UrlResponse buildResponse(String shortCode) {
        return UrlResponse.builder()
                .shortCode(shortCode)
                .shortUrl("http://localhost:8080/" + shortCode)
                .build();
    }
}
