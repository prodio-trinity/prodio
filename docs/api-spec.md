# Prodio API 명세

공통 응답 포맷:
```json
{ "success": true, "data": { ... } }
{ "success": false, "message": "...", "errorCode": "..." }
```

---

## catalog 모듈

### 거래처 (Client)

| 메서드 | URL | 설명 | 권한 |
|--------|-----|------|------|
| `GET` | `/api/catalog/clients` | 목록 조회 | STAFF, ADMIN |
| `GET` | `/api/catalog/clients/{id}` | 상세 조회 | STAFF, ADMIN |
| `POST` | `/api/catalog/clients` | 등록 | STAFF, ADMIN |
| `PUT` | `/api/catalog/clients/{id}` | 수정 | STAFF, ADMIN |
| `DELETE` | `/api/catalog/clients/{id}` | 삭제 | ADMIN |
| `GET` | `/api/catalog/clients/autocomplete?q=` | 자동완성 (이름 검색) | STAFF, ADMIN |
| `POST` | `/api/catalog/clients/bulk` | 엑셀 일괄 업로드 | ADMIN |

**GET `/api/catalog/clients`** 쿼리 파라미터:
- `q` (string, optional) — 회사명 검색
- `page` (int, default 0)
- `size` (int, default 20)

**POST `/api/catalog/clients`** 요청 바디:
```json
{
  "companyName": "ABC 제조",
  "representative": "홍길동",
  "address": "서울시 강남구",
  "phone": "010-1234-5678"
}
```

**GET `/api/catalog/clients/autocomplete?q=ABC`** 응답:
```json
{
  "success": true,
  "data": [
    { "id": "1", "companyName": "ABC 제조", "phone": "010-1234-5678" }
  ]
}
```
최대 10건 반환.

---

### 품목 (Product)

| 메서드 | URL | 설명 | 권한 |
|--------|-----|------|------|
| `GET` | `/api/catalog/products` | 목록 조회 | STAFF, ADMIN |
| `GET` | `/api/catalog/products/{id}` | 상세 조회 | STAFF, ADMIN |
| `POST` | `/api/catalog/products` | 등록 | STAFF, ADMIN |
| `PUT` | `/api/catalog/products/{id}` | 수정 | STAFF, ADMIN |
| `DELETE` | `/api/catalog/products/{id}` | 삭제 | ADMIN |
| `GET` | `/api/catalog/products/autocomplete?q=` | 자동완성 (품목명 검색) | STAFF, ADMIN |

**POST `/api/catalog/products`** 요청 바디:
```json
{
  "name": "A형 부품",
  "description": "설명",
  "unitPrice": 15000
}
```

---

## order 모듈

### 수주 (Order)

| 메서드 | URL | 설명 | 권한 |
|--------|-----|------|------|
| `GET` | `/api/orders` | 목록 조회 | STAFF, ADMIN |
| `GET` | `/api/orders/{id}` | 상세 조회 | STAFF, ADMIN |
| `POST` | `/api/orders` | 수주 등록 (상태: PENDING) | STAFF, ADMIN |
| `PATCH` | `/api/orders/{id}/start-production` | 생산 시작 (PENDING → IN_PRODUCTION) | STAFF, ADMIN |
| `PATCH` | `/api/orders/{id}/payment` | 입금 확인 토글 | STAFF, ADMIN |

**GET `/api/orders`** 쿼리 파라미터:
- `status` (string, optional) — PENDING, IN_PRODUCTION
- `q` (string, optional) — 거래처명 또는 품목명 검색
- `page` (int, default 0)
- `size` (int, default 20)

**POST `/api/orders`** 요청 바디:
```json
{
  "clientId": "1",
  "productId": "2",
  "quantity": 100,
  "vatIncluded": false,
  "dueDate": "2026-09-01",
  "deliveryAddress": "경기도 수원시",
  "note": "포장 주의"
}
```

응답 (`data`):
```json
{
  "id": "3",
  "clientId": "1",
  "clientName": "ABC 제조",
  "productId": "2",
  "productName": "A형 부품",
  "quantity": 100,
  "unitPrice": 15000,
  "vatIncluded": false,
  "totalAmount": 1500000,
  "dueDate": "2026-09-01",
  "deliveryAddress": "경기도 수원시",
  "status": "PENDING",
  "paymentConfirmed": false,
  "note": "포장 주의",
  "createdAt": "2026-08-17T00:00:00Z"
}
```

**PATCH `/api/orders/{id}/start-production`** — 바디 없음. 내부적으로 `OrderCreated` 이벤트 발행.

**PATCH `/api/orders/{id}/payment`** 요청 바디:
```json
{ "confirmed": true }
```

---

## production 모듈

### 생산 기록 (ProductionRecord)

| 메서드 | URL | 설명 | 권한 |
|--------|-----|------|------|
| `GET` | `/api/production` | 생산 목록 조회 | STAFF, ADMIN |
| `GET` | `/api/production/{id}` | 생산 상세 조회 | STAFF, ADMIN |
| `PATCH` | `/api/production/{id}/ship` | 배송 시작 (IN_PRODUCTION → IN_DELIVERY) + SMS | STAFF, ADMIN |
| `PATCH` | `/api/production/{id}/complete` | 완료 처리 (IN_DELIVERY → COMPLETED) | STAFF, ADMIN |

> ProductionRecord는 `OrderCreated` 이벤트 수신 시 자동 생성됨. 별도 POST API 없음.

**GET `/api/production`** 쿼리 파라미터:
- `status` (string, optional) — IN_PRODUCTION, IN_DELIVERY, COMPLETED
- `page` (int, default 0)
- `size` (int, default 20)

응답 (`data.records` 항목):
```json
{
  "id": "10",
  "orderId": "3",
  "clientName": "ABC 제조",
  "productName": "A형 부품",
  "quantity": 100,
  "dueDate": "2026-09-01",
  "status": "IN_PRODUCTION",
  "startedAt": "2026-08-17T09:00:00Z",
  "shippedAt": null,
  "completedAt": null
}
```

**PATCH `/api/production/{id}/ship`** — 바디 없음. SMS 자동 발송 + `OrderShipped` 이벤트 발행.

**PATCH `/api/production/{id}/complete`** — 바디 없음. `OrderCompleted` 이벤트 발행.

---

## statistics 모듈

| 메서드 | URL | 설명 | 권한 |
|--------|-----|------|------|
| `GET` | `/api/statistics/overview` | 주요 지표 요약 | ADMIN |
| `GET` | `/api/statistics/orders` | 수주 목록 (전체 상태) | ADMIN |
| `GET` | `/api/statistics/products` | 품목별 통계 | ADMIN |
| `POST` | `/api/statistics/ai-summary` | AI 요약 요청 (Spring AI + Ollama) | ADMIN |

**GET `/api/statistics/overview`** 쿼리 파라미터:
- `from` (date, optional) — 시작일
- `to` (date, optional) — 종료일

응답 (`data`):
```json
{
  "totalOrders": 120,
  "pendingCount": 10,
  "inProductionCount": 30,
  "inDeliveryCount": 15,
  "completedCount": 65,
  "onTimeRate": 0.89,
  "totalRevenue": 48000000,
  "unpaidCount": 8
}
```

**GET `/api/statistics/orders`** 쿼리 파라미터:
- `status` (string, optional) — PENDING, IN_PRODUCTION, IN_DELIVERY, COMPLETED
- `from` / `to` (date, optional)
- `page`, `size`

> 이 엔드포인트가 대시보드 칸반 보드 데이터 소스. statistics_order_view 조회.

**GET `/api/statistics/products`** 쿼리 파라미터:
- `from` / `to` (date, optional)

응답 (`data`):
```json
[
  { "productId": "2", "productName": "A형 부품", "totalQuantity": 500, "totalRevenue": 7500000, "orderCount": 12 }
]
```

**POST `/api/statistics/ai-summary`** — 바디 없음. 현재 생산 현황 데이터를 LLM에 전달해 자연어 요약 반환.
```json
{ "success": true, "data": { "summary": "이번 주 수주 건수는 ..." } }
```

---

## 이벤트 페이로드 참고

| 이벤트 | 발행 시점 | 구독 모듈 | 주요 필드 |
|--------|----------|----------|----------|
| `OrderCreated` | 수주 생산 시작 | production, statistics | orderId, clientId, clientName, clientPhone, productId, productName, quantity, totalAmount, dueDate |
| `OrderShipped` | 배송 시작 | statistics | orderId, productionRecordId, shippedAt, clientPhone |
| `OrderCompleted` | 완료 처리 | statistics | orderId, productionRecordId, completedAt, dueDate |
