-- ============================================================
-- production 모듈 — 소유권 확인용 client_id 추가
-- (고객이 자기 주문의 생산 기록만 조회할 수 있어야 하는데, 지금은 그 주문이
--  어느 거래처 것인지 알 방법이 없어서 추가한다. OrderConfirmedEvent에 이미
--  실려오는 clientId를 생성 시점에 스냅샷으로 저장한다.)
-- ============================================================

ALTER TABLE production_records ADD COLUMN client_id BIGINT;

CREATE INDEX idx_production_records_client_id ON production_records (client_id);
