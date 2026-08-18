# Prodio — 생산 관리 서비스

수주 등록부터 생산·배송·완료까지의 전 과정을 관리하는 생산 관리 시스템.

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| 프론트엔드 | Next.js 16 |
| 백엔드 | Spring Boot 4 + Spring Modulith |
| DB | PostgreSQL 16 |
| AI | Gemini API |
| SMS | Coolsms |
| 배포 | Docker + GitHub Actions + Oracle Cloud |

---

## 로컬 개발 환경 세팅

### 사전 준비

- Java 25
- Node.js 20+
- Docker Desktop

### 1. 저장소 클론

```bash
git clone https://github.com/prodio-trinity/prodio.git
cd prodio
```

### 2. 로컬 DB 실행

```bash
docker compose -f docker-compose.local.yml up -d
```

- PostgreSQL이 `localhost:5432`로 뜸
- DB/계정 정보는 `application-local.yaml`에 이미 세팅되어 있으니 별도 설정 불필요

### 3. 환경변수 설정

`backend/.env` 파일 생성 후 슬랙에 공유된 값 붙여넣기:

```
GEMINI_API_KEY=슬랙_공유값
```

### 4. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

> IntelliJ 사용 시: Run Configuration → Environment Variables에 `GEMINI_API_KEY=슬랙_공유값` 추가

- 서버: `http://localhost:8080`
- Flyway가 자동으로 DB 마이그레이션 실행 (V1, V2)
- 초기 관리자 계정: `admin@prodio.com` / `admin1234`

### 5. 프론트엔드 실행

`frontend/.env.local` 파일 생성:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

```bash
cd frontend
npm install
npm run dev
```

- 브라우저: `http://localhost:3000`

---

## 브랜치 전략

```
main       — 배포 브랜치 (직접 push 금지)
feature/*  — 기능 개발
```

`main`에 PR 머지되면 GitHub Actions가 자동으로 빌드 → 서버 배포.

---

## 배포 서버

| 항목 | 값 |
|------|----|
| URL | https://prodio.songkyeongyong.xyz |
| 서버 | Oracle Cloud (ubuntu@168.107.7.18) |
| 관리자 계정 | `admin@prodio.com` / `admin1234` |

---

## 도메인 구조

| 모듈 | 담당 기능 |
|------|-----------|
| `catalog` | 거래처·품목 CRUD, 자동완성 |
| `order` | 수주 등록, 상태 관리, 입금 확인 |
| `production` | 생산·배송 처리, SMS 발송 |
| `statistics` | Read Model, 통계 대시보드, AI 요약 |

모듈 간 JPA 연관관계 금지 — ID 참조 + 도메인 이벤트로만 통신.

---

## API 명세

`docs/api-spec.md` 참고
