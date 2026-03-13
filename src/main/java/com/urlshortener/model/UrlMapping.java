package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a URL mapping stored in the database.
 * Maps to the "url_mappings" table.
 */
@Entity
@Table(
    name = "url_mappings",
    indexes = {
        @Index(name = "idx_short_code", columnList = "shortCode", unique = true),
        @Index(name = "idx_original_url", columnList = "originalUrl")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The original long URL provided by the user. */
    @Column(nullable = false, length = 2048)
    private String originalUrl;

    /** The unique short code (e.g., "aB3xZ9"). */
    @Column(nullable = false, unique = true, length = 20)
    private String shortCode;

    /** How many times this short URL has been accessed. */
    @Column(nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    /** Timestamp of when this mapping was created. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Optional expiry time — null means never expires. */
    @Column
    private LocalDateTime expiresAt;

    /** Whether this mapping is currently active. */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Automatically set createdAt before first persist. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
