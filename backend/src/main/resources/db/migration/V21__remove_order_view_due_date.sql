-- ============================================================
-- statistics 모듈 — order 모듈의 due_date 제거를 반영
-- order 모듈이 orders.due_date를 완전히 없애면서(V20), 납기 이행률(on_time)의
-- 데이터 원천이 사라졌다. statistics_order_view에서도 due_date/on_time을 제거한다.
-- ============================================================

DROP INDEX IF EXISTS idx_stats_view_due_date;

ALTER TABLE statistics_order_view DROP COLUMN IF EXISTS due_date;
ALTER TABLE statistics_order_view DROP COLUMN IF EXISTS on_time;
