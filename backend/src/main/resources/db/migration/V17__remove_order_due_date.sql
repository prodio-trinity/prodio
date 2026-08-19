-- 납기 일정은 Order가 결정하거나 소유하지 않으므로 수주 모델에서 제거한다.
ALTER TABLE orders DROP COLUMN due_date;
