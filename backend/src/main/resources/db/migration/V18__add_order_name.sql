-- 사용자가 주문을 구분하기 위해 붙이는 선택 이름. 비어 있으면 화면에서 품목명을 사용한다.
ALTER TABLE orders ADD COLUMN order_name VARCHAR(200);
