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
원본 URL로 301 리다이렉트됨.

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
1. 원본 URL을 SHA-256으로 해싱
2. 해시값을 Base62로 인코딩하여 7자리 단축 URL 생성
3. 데이터베이스에서 중복 확인
4. 중복 시 랜덤 문자열로 재생성 (최대 10회 시도)

### 알고리즘 비교 분석

| 구분 | 해시 기반 (현재 구현)                                                                    | 순수 랜덤                                             |
|------|----------------------------------------------------------------------------------|---------------------------------------------------|
| **장점** | - 동일 URL → 동일 해시 (일관성)<br>- 해시 함수 분산으로 충돌 적음<br>• 중복 URL 재사용 가능<br>• 디버깅 시 추적 용이 | - 패턴 예측 불가 (보안성)<br>- 구현 단순<br>• 균등한 문자 분포        |
| **단점** | - 해시 추측으로 URL 유추 가능<br>- SHA-256 연산 오버헤드<br>• 충돌 시 복잡한 처리                        | - URL 증가 시 충돌 급증<br>• 대량 데이터에서 성능 저하<br>- 디버깅 어려움 |
| **시간 복잡도** | O(1) + 해시 계산                                                                     | O(1) + 중복 검사                                      |
| **충돌 확률**<br>(10만 URL) | 약 0.00001%                                                                       | 약 0.00001%                                        |
| **충돌 확률**<br>(100만 URL) | 약 0.01%                                                                          | 약 0.01%                                           |
| **적합한 규모** | 중대규모 (10만~100만)                                                                  | 소규모 (~10만)                                        |

- 소규모 서비스 (10만 URL 이하): 순수 랜덤 방식을 권장함. 충돌 확률이 낮고 구현이 단순함.
- 중대규모 서비스 (10만~100만 URL): 현재의 해시 기반 + 랜덤 백업 방식을 권장함. 일관성을 제공함.
- 대규모 서비스 (100만 URL 이상): 카운터 기반 방식이나 분산 ID 생성 방식을 고려해야 함.

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
- **Testing**: JUnit 5

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