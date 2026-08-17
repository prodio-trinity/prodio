# Prodio Backend

생산 관리 서비스 백엔드 — Spring Boot 4.1.0 + Spring Modulith

## 기술 스택

- Java 25 / Spring Boot 4.1.0
- Spring Modulith (Modular Monolith)
- Spring Security
- Spring Data JPA + Flyway
- PostgreSQL
- Coolsms (SMS 발송)

## 모듈 구조

```
com.prodio/
  catalog/       클라이언트 관리, 제품 관리
  order/         수주 관리, 칸반 보드
  production/    생산 일정 및 기록
  statistics/    통계 대시보드
  infra/         AI(Gemini), SMS(Coolsms) 연동
  shared/        ApiResponse, 공통 ValidationExceptionHandler
  config/        SecurityConfig
```

모듈 간 직접 참조 금지 — 반드시 도메인 이벤트(`@ApplicationModuleListener`)로 통신

## 로컬 실행

### 1. DB 띄우기

```bash
docker compose -f docker-compose.local.yml up -d
```

### 2. 애플리케이션 실행

IntelliJ에서 `ProdioApplication` 실행 (프로파일: `local` 자동 적용)

또는 터미널:

```bash
./gradlew bootRun
```

### 3. 확인

```
http://localhost:8080/actuator/health
```

## API 테스트

`.http/` 폴더에 도메인별 요청 파일 있음. IntelliJ HTTP Client로 바로 실행 가능.

## 테스트 실행

```bash
./gradlew test
```

`ModuleBoundaryTest` — 모듈 경계 위반 시 빌드 실패

## Flyway 마이그레이션

`src/main/resources/db/migration/` 폴더에 작성.  
네이밍: `V{숫자}__{설명}.sql` (예: `V2__create_client_table.sql`)  
버전 번호는 팀원끼리 사전에 조율 후 작성할 것 — 번호 충돌 시 Flyway 실행 실패.

## 환경변수 (운영)

| 변수명 | 설명 |
|--------|------|
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | DB 유저명 |
| `DB_PASSWORD` | DB 비밀번호 |
| `DB_DRIVER_CLASS_NAME` | `org.postgresql.Driver` |
| `GEMINI_API_KEY` | Google AI Studio API 키 |
| `SPRING_PROFILES_ACTIVE` | `prod` |
