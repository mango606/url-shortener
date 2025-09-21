package com.urlshortener;

import com.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class UrlShortenerTest {

    private static final Logger log = LoggerFactory.getLogger(UrlShortenerTest.class);

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Test
    public void testKpopUrls() {
        System.out.println("\n=== 유튜브 URL 단축 테스트 ===\n");

        // 테스트할 YouTube URL들
        String[] kpopUrls = {
                "https://www.youtube.com/watch?v=eny0BqmSwmM",
                "https://www.youtube.com/watch?v=7vCK0VBuQLs&pp=ygUM7ZeM7Yq466at7Iqk",
                "https://www.youtube.com/watch?v=BuSEXqdD5FE"
        };

        String[] descriptions = {
                "유튜브 - 첫 번째 영상",
                "유튜브 - 두 번째 영상 (검색 파라미터 포함)",
                "유튜브 - 세 번째 영상"
        };

        System.out.printf("%-40s | %-50s | %-15s%n", "설명", "원본 URL", "단축 URL");
        System.out.println("=" .repeat(110));

        for (int i = 0; i < kpopUrls.length; i++) {
            try {
                String originalUrl = kpopUrls[i];
                String shortUrl = urlShortenerService.shortenUrl(originalUrl);
                String fullShortUrl = "http://localhost:8080/" + shortUrl;

                System.out.printf("%-40s | %-50s | %-15s%n",
                        descriptions[i],
                        originalUrl.length() > 47 ? originalUrl.substring(0, 44) + "..." : originalUrl,
                        shortUrl
                );

                // URL 확장 테스트
                Optional<String> expandedUrl = urlShortenerService.expandUrl(shortUrl);
                if (expandedUrl.isPresent() && expandedUrl.get().equals(originalUrl)) {
                    log.info("✅ URL 확장 성공: {} -> {}", shortUrl, expandedUrl.get());
                } else {
                    log.error("❌ URL 확장 실패: {}", shortUrl);
                }

            } catch (Exception e) {
                System.out.printf("%-40s | %-50s | %-15s%n",
                        descriptions[i],
                        "오류: " + e.getMessage(),
                        "실패"
                );
                log.error("URL 단축 실패: {}", kpopUrls[i], e);
            }
        }

        System.out.println("\n=== 중복 URL 테스트 ===");
        String duplicateUrl = "https://www.youtube.com/watch?v=yebNIHKAC4A";
        String shortUrl1 = urlShortenerService.shortenUrl(duplicateUrl);
        String shortUrl2 = urlShortenerService.shortenUrl(duplicateUrl);

        System.out.printf("첫 번째 단축: %s -> %s%n", duplicateUrl, shortUrl1);
        System.out.printf("두 번째 단축: %s -> %s%n", duplicateUrl, shortUrl2);
        System.out.printf("중복 방지 테스트: %s%n", shortUrl1.equals(shortUrl2) ? "✅ 성공 (같은 단축 URL 반환)" : "❌ 실패");

        System.out.println("\n=== 터미널 출력 완료 ===");
    }

    @Test
    public void testUrlValidation() {
        System.out.println("\n=== URL 유효성 검사 테스트 ===");

        String[] invalidUrls = {
                "invalid-url",
                "ftp://example.com",
                "",
                "www.youtube.com"
        };

        for (String invalidUrl : invalidUrls) {
            try {
                urlShortenerService.shortenUrl(invalidUrl);
                System.out.printf("❌ 유효하지 않은 URL이 통과됨: %s%n", invalidUrl);
            } catch (IllegalArgumentException e) {
                System.out.printf("✅ 유효성 검사 성공: %s -> %s%n", invalidUrl, e.getMessage());
            }
        }
    }
}