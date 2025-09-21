package com.urlshortener.controller;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody Map<String, String> request) {
        try {
            String originalUrl = request.get("url");
            if (originalUrl == null || originalUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "URL이 필요합니다."));
            }

            String shortUrl = urlShortenerService.shortenUrl(originalUrl.trim());

            return ResponseEntity.ok(Map.of(
                    "originalUrl", originalUrl,
                    "shortUrl", shortUrl,
                    "fullShortUrl", "http://localhost:8080/" + shortUrl
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("URL 단축 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortUrl) {
        Optional<String> originalUrl = urlShortenerService.expandUrl(shortUrl);

        if (originalUrl.isPresent()) {
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .location(URI.create(originalUrl.get()))
                    .build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/stats/{shortUrl}")
    public ResponseEntity<?> getUrlStats(@PathVariable String shortUrl) {
        UrlMapping urlMapping = urlShortenerService.getUrlStats(shortUrl);

        if (urlMapping != null) {
            return ResponseEntity.ok(Map.of(
                    "shortUrl", urlMapping.getShortUrl(),
                    "originalUrl", urlMapping.getOriginalUrl(),
                    "clickCount", urlMapping.getClickCount(),
                    "createdAt", urlMapping.getCreatedAt(),
                    "expiresAt", urlMapping.getExpiresAt(),
                    "isExpired", urlMapping.isExpired()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}