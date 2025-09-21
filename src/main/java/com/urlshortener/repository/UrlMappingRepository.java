package com.urlshortener.repository;

import com.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortUrl(String shortUrl);

    Optional<UrlMapping> findByOriginalUrl(String originalUrl);

    @Query("SELECT u FROM UrlMapping u WHERE u.shortUrl = :shortUrl AND u.expiresAt > :now")
    Optional<UrlMapping> findByShortUrlAndNotExpired(@Param("shortUrl") String shortUrl,
                                                     @Param("now") LocalDateTime now);

    @Query("DELETE FROM UrlMapping u WHERE u.expiresAt < :now")
    void deleteExpiredUrls(@Param("now") LocalDateTime now);

    boolean existsByShortUrl(String shortUrl);
}