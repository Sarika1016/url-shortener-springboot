package com.urlshortener.repository;

import com.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for UrlMapping entities.
 *
 * Spring auto-generates SQL from method names at runtime:
 *   findByShortCode  → SELECT * FROM url_mappings WHERE short_code = ?
 *   existsByShortCode → SELECT COUNT(*) > 0 FROM url_mappings WHERE short_code = ?
 */
@Repository
public interface UrlRepository extends JpaRepository<UrlMapping, Long> {

    /** Find a mapping by its short code. */
    Optional<UrlMapping> findByShortCode(String shortCode);

    /** Check if a short code is already taken (collision check). */
    boolean existsByShortCode(String shortCode);

    /** Find by original URL to avoid duplicate entries. */
    Optional<UrlMapping> findByOriginalUrlAndActiveTrue(String originalUrl);

    /**
     * Atomically increment the click counter for a given short code.
     * Uses JPQL — avoids loading the entity just to increment a counter.
     */
    @Modifying
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);
}
