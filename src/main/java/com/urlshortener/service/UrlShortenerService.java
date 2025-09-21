package com.urlshortener.service;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

    private final UrlMappingRepository urlMappingRepository;
    private final SecureRandom random = new SecureRandom();

    // Base62 문자셋 (0-9, A-Z, a-z)
    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SHORT_URL_LENGTH = 7;
    private static final int MAX_RETRY_COUNT = 10;

    @Transactional
    public String shortenUrl(String originalUrl) {
        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("유효하지 않은 URL입니다: " + originalUrl);
        }

        // 단축된 URL이 있는지 확인 (중복 방지)
        Optional<UrlMapping> existing = urlMappingRepository.findByOriginalUrl(originalUrl);
        if (existing.isPresent() && !existing.get().isExpired()) {
            return existing.get().getShortUrl();
        }

        String shortUrl = generateShortUrl(originalUrl);

        UrlMapping urlMapping = UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortUrl(shortUrl)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusYears(1))
                .clickCount(0L)
                .build();

        urlMappingRepository.save(urlMapping);
        return shortUrl;
    }

    @Cacheable(value = "urlMappings", key = "#shortUrl")
    @Transactional
    public Optional<String> expandUrl(String shortUrl) {
        Optional<UrlMapping> urlMapping = urlMappingRepository
                .findByShortUrlAndNotExpired(shortUrl, LocalDateTime.now());

        if (urlMapping.isPresent()) {
            UrlMapping mapping = urlMapping.get();
            mapping.incrementClickCount();
            urlMappingRepository.save(mapping);
            return Optional.of(mapping.getOriginalUrl());
        }

        return Optional.empty();
    }

    private String generateShortUrl(String originalUrl) {
        // Hash 기반 접근법과 랜덤 생성의 하이브리드
        String hashBased = generateHashBasedShortUrl(originalUrl);

        // 충돌 체크 및 재시도
        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            String candidate = (i == 0) ? hashBased : generateRandomShortUrl();

            // 길이 검증 추가
            if (candidate.length() > SHORT_URL_LENGTH) {
                candidate = candidate.substring(0, SHORT_URL_LENGTH);
            }

            if (!urlMappingRepository.existsByShortUrl(candidate)) {
                return candidate;
            }
        }

        throw new RuntimeException("단축 URL 생성에 실패했습니다. 다시 시도해주세요.");
    }

    private String generateHashBasedShortUrl(String originalUrl) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(originalUrl.getBytes(StandardCharsets.UTF_8));

            // 해시값을 Base62로 인코딩
            long hashLong = 0;
            for (int i = 0; i < 8 && i < hash.length; i++) {
                hashLong = (hashLong << 8) | (hash[i] & 0xff);
            }

            return base62Encode(Math.abs(hashLong), SHORT_URL_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 해시 생성 실패, 랜덤 생성으로 대체", e);
            return generateRandomShortUrl();
        }
    }

    private String generateRandomShortUrl() {
        StringBuilder sb = new StringBuilder(SHORT_URL_LENGTH);
        for (int i = 0; i < SHORT_URL_LENGTH; i++) {
            sb.append(BASE62_ALPHABET.charAt(random.nextInt(BASE62_ALPHABET.length())));
        }
        return sb.toString();
    }

    private String base62Encode(long number, int length) {
        StringBuilder sb = new StringBuilder();
        while (number > 0 && sb.length() < length) {
            sb.append(BASE62_ALPHABET.charAt((int) (number % 62)));
            number /= 62;
        }

        // 길이 맞추기 (앞에 랜덤 문자 추가)
        while (sb.length() < length) {
            sb.append(BASE62_ALPHABET.charAt(random.nextInt(62)));
        }

        return sb.reverse().toString().substring(0, SHORT_URL_LENGTH);
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public UrlMapping getUrlStats(String shortUrl) {
        return urlMappingRepository.findByShortUrl(shortUrl)
                .orElse(null);
    }
}