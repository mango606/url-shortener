package com.urlshortener;

import com.urlshortener.service.UrlShortenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoApplicationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoApplicationRunner.class);
    private final UrlShortenerService urlShortenerService;

    public DemoApplicationRunner(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println();
        System.out.println("KPop Demon Hunters URL 단축기");
        System.out.println("=" .repeat(80));

        String[] youtubeUrls = {
                "https://www.youtube.com/watch?v=eny0BqmSwmM",
                "https://www.youtube.com/watch?v=7vCK0VBuQLs&pp=ygUM7ZeM7Yq566at7Iqk",
                "https://www.youtube.com/watch?v=BuSEXqdD5FE",
                "https://www.youtube.com/watch?v=yebNIHKAC4A"
        };

        String[] videoNames = {
                "뮤직비디오 #1",
                "뮤직비디오 #2",
                "뮤직비디오 #3",
                "뮤직비디오 #4"
        };

        for (int i = 0; i < youtubeUrls.length; i++) {
            try {
                String shortCode = urlShortenerService.shortenUrl(youtubeUrls[i]);
                System.out.printf("%s - %s -> %s%n",
                        videoNames[i],
                        youtubeUrls[i],
                        shortCode);
            } catch (Exception e) {
                System.out.printf("%s - %s -> 오류: %s%n",
                        videoNames[i],
                        youtubeUrls[i],
                        e.getMessage());
            }
        }

        System.out.println();
        System.out.println("단축 URL 사용법:");
        System.out.println("  브라우저: http://localhost:8080/{단축코드}");
        System.out.println("  통계조회: http://localhost:8080/api/stats/{단축코드}");
        System.out.println();
        System.out.println("서버 준비 완료. 브라우저에서 단축 URL을 테스트해보세요.");
        System.out.println("H2 데이터베이스 콘솔: http://localhost:8080/h2-console");
        System.out.println("JDBC URL: jdbc:h2:mem:testdb, 사용자: sa, 비밀번호: (공백)");
        System.out.println("=" .repeat(80));
    }
}