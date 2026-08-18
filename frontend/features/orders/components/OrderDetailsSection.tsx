"use client";

import styles from "./orders.module.css";

type OrderDetailsSectionProps = {
  quantity: number;
  dueDate: string;
  deliveryAddress: string;
  vatIncluded: boolean;
  note: string;
  onQuantityChange: (quantity: number) => void;
  onDueDateChange: (dueDate: string) => void;
  onDeliveryAddressChange: (deliveryAddress: string) => void;
  onVatIncludedChange: (vatIncluded: boolean) => void;
  onNoteChange: (note: string) => void;
};

/** 카탈로그 선택 이후 Order가 소유하는 주문 조건 입력 영역. */
export function OrderDetailsSection({
  quantity,
  dueDate,
  deliveryAddress,
  vatIncluded,
  note,
  onQuantityChange,
  onDueDateChange,
  onDeliveryAddressChange,
  onVatIncludedChange,
  onNoteChange,
}: OrderDetailsSectionProps) {
  return (
    <>
      <section className={styles.card}>
        <h2>2. 납품 및 금액</h2>
        <div className={styles.grid}>
          <label>
            수량
            <input type="number" min="1" value={quantity} onChange={(event) => onQuantityChange(Number(event.target.value))} required />
          </label>
          <label>
            납기일
            <input type="date" value={dueDate} onChange={(event) => onDueDateChange(event.target.value)} required />
          </label>
          <label className={styles.wide}>
            납품 주소
            <input value={deliveryAddress} onChange={(event) => onDeliveryAddressChange(event.target.value)} placeholder="선택 입력" />
          </label>
        </div>
        <label className={styles.check}>
          <input type="checkbox" checked={vatIncluded} onChange={(event) => onVatIncludedChange(event.target.checked)} /> 부가세 10% 포함
        </label>
      </section>
      <section className={styles.card}>
        <h2>3. 메모</h2>
        <textarea rows={4} value={note} onChange={(event) => onNoteChange(event.target.value)} placeholder="생산 또는 납품 시 참고할 내용을 입력하세요." />
      </section>
    </>
  );
}
