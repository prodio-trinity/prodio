"use client";

import styles from "./orders.module.css";

type OrderDetailsSectionProps = {
  dueDate: string;
  vatIncluded: boolean;
  onDueDateChange: (dueDate: string) => void;
  onVatIncludedChange: (vatIncluded: boolean) => void;
};

/** 카탈로그 선택 이후 Order가 소유하는 주문 조건 입력 영역. */
export function OrderDetailsSection({
  dueDate,
  vatIncluded,
  onDueDateChange,
  onVatIncludedChange,
}: OrderDetailsSectionProps) {
  return (
      <section className={styles.card}>
        <h2>2. 납기 및 금액</h2>
        <div className={styles.grid}>
          <label>
            납기일
            <input type="date" value={dueDate} onChange={(event) => onDueDateChange(event.target.value)} required />
          </label>
        </div>
        <label className={styles.check}>
          <input type="checkbox" checked={vatIncluded} onChange={(event) => onVatIncludedChange(event.target.checked)} /> 부가세 10% 포함
        </label>
      </section>
  );
}

export function OrderMemoSection({ note, onNoteChange }: {
  note: string;
  onNoteChange: (note: string) => void;
}) {
  return <section className={styles.card}>
    <h2>4. 메모</h2>
    <textarea rows={4} value={note} onChange={(event) => onNoteChange(event.target.value)} placeholder="생산 또는 납품 시 참고할 내용을 입력하세요." />
  </section>;
}
