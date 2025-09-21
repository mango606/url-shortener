# URL 단축기 서비스

> Spring Boot로 구현한 실제 동작하는 URL 단축 서비스

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-8.14.3-blue?logo=gradle&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2-Database-blue?logo=h2&logoColor=white)

## 1. 확인 페이지
- 메인 서비스: http://localhost:8080
- H2 데이터베이스 콘솔: http://localhost:8080/h2-console
    - JDBC URL: `jdbc:h2:mem:testdb`
    - Username: `sa`
    - Password: (빈칸)

## 2. 테스트 데이터

서비스 시작 시 자동으로 다음 YouTube 링크들이 단축 처리됨.

1. `https://www.youtube.com/watch?v=eny0BqmSwmM`
2. `https://www.youtube.com/watch?v=7vCK0VBuQLs&pp=ygUM7ZeM7Yq566at7Iqk`
3. `https://www.youtube.com/watch?v=BuSEXqdD5FE`
4. `https://www.youtube.com/watch?v=yebNIHKAC4A`

## 3. API 사용법

### URL 단축
```bash
POST /api/shorten
Content-Type: application/json

{
  "url": "https://www.youtube.com/watch?v=yebNIHKAC4A"
}
```

응답:
```json
{
  "originalUrl": "https://www.youtube.com/watch?v=yebNIHKAC4A",
  "shortUrl": "Ab3Cd9F",
  "fullShortUrl": "http://localhost:8080/Ab3Cd9F"
}
```

### URL 리다이렉트
```bash
GET /{shortUrl}
```
원본 URL로 301 리다이렉트됩니다.

### 통계 조회
```bash
GET /api/stats/{shortUrl}
```

응답:
```json
{
  "shortUrl": "Ab3Cd9F",
  "originalUrl": "https://www.youtube.com/watch?v=yebNIHKAC4A",
  "clickCount": 5,
  "createdAt": "2024-12-19T10:30:00",
  "expiresAt": "2025-12-19T10:30:00",
  "isExpired": false
}
```

## 4. 시스템 구조

### URL 생성 알고리즘
- **해시 기반**: SHA-256 해시를 Base62로 변환
- **충돌 방지**: SecureRandom을 이용한 재생성
- **안정성**: 최대 10회 재시도 메커니즘

### 데이터 모델
```sql
CREATE TABLE url_mappings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_url VARCHAR(2048) NOT NULL,
    short_url VARCHAR(15) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    click_count BIGINT DEFAULT 0
);
```

### 주요 기능
- **URL 검증**: HTTP/HTTPS 프로토콜 및 형식 확인
- **중복 처리**: 동일 원본 URL에 대해 같은 단축 URL 반환
- **만료 관리**: 기본 1년 만료 정책
- **클릭 추적**: 실시간 통계 수집 및 조회
- **캐싱**: Spring Cache를 통한 성능 최적화

## 기술 스택

- **Framework**: Spring Boot 3.2.0
- **Build Tool**: Gradle
- **Database**: H2
- **Cache**: Spring Cache
- **Java**: 17

## 실행 결과 예시

```
KPop Demon Hunters URL 단축기
================================================================================
뮤직비디오 #1 - https://www.youtube.com/watch?v=eny0BqmSwmM -> qLFC56J
뮤직비디오 #2 - https://www.youtube.com/watch?v=7vCK0VBuQLs&pp=ygUM7ZeM7Yq566at7Iqk -> Lvio5bF
뮤직비디오 #3 - https://www.youtube.com/watch?v=BuSEXqdD5FE -> dqs24ks
뮤직비디오 #4 - https://www.youtube.com/watch?v=yebNIHKAC4A -> q60Zica

단축 URL 사용법:
  브라우저: http://localhost:8080/{단축코드}
  통계조회: http://localhost:8080/api/stats/{단축코드}

서버 준비 완료. 브라우저에서 단축 URL을 테스트해보세요.
H2 데이터베이스 콘솔: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb, 사용자: sa, 비밀번호: (공백)
================================================================================
```