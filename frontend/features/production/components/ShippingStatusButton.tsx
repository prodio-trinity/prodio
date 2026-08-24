"use client";

import { useState } from "react";
import { productionService } from "../services/productionService";
import { PRODUCTION_STATUS_LABELS, type ProductionRecord } from "../types/production";
import styles from "./ShippingStatusButton.module.css";

function formatDate(value: string | null) {
  if (!value) return "-";
  return new Date(value).toLocaleString("ko-KR", { dateStyle: "short", timeStyle: "short" });
}

const STEPS: { key: keyof Pick<ProductionRecord, "startedAt" | "shippedAt" | "completedAt">; label: string }[] = [
  { key: "startedAt", label: "생산 시작" },
  { key: "shippedAt", label: "배송 시작" },
  { key: "completedAt", label: "배송 완료" },
];

/**
 * 주문번호 하나로 동작하는 배송 현황 버튼. 클릭하면 자체적으로 생산 정보를 불러와 모달로 보여준다.
 * 생산 레코드가 아직 없는 주문(입금 대기 등)에서는 굳이 렌더링하지 않아도 되고,
 * 혹시 그런 상태에서 눌리더라도 "아직 시작되지 않았다"는 안내로 처리한다.
 */
export function ShippingStatusButton({ orderId }: { orderId: string }) {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [record, setRecord] = useState<ProductionRecord | null>(null);

  async function openModal() {
    setOpen(true);
    setLoading(true);
    setError("");
    try {
      const result = await productionService.getByOrderId(orderId);
      setRecord(result);
    } catch (cause) {
      setRecord(null);
      setError(
        cause instanceof Error && cause.message.includes("존재하지 않는")
          ? "아직 생산이 시작되지 않은 주문입니다."
          : cause instanceof Error
            ? cause.message
            : "배송 현황을 불러오지 못했습니다.",
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <button type="button" className={styles.trigger} onClick={() => void openModal()}>
        배송 현황
      </button>

      {open && (
        <div
          className={styles.modalBackdrop}
          role="presentation"
          onMouseDown={() => setOpen(false)}
        >
          <section
            className={styles.modal}
            role="dialog"
            aria-modal="true"
            aria-label="배송 현황"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <div>
              <span className={styles.eyebrow}>SHIPPING STATUS</span>
              <h2>배송 현황</h2>
              <p>주문번호 #{orderId}</p>
            </div>

            {loading ? (
              <p className={styles.empty}>불러오는 중...</p>
            ) : error ? (
              <p className={styles.error}>{error}</p>
            ) : record ? (
              <>
                <span className={styles.statusBadge}>{PRODUCTION_STATUS_LABELS[record.status]}</span>
                <ol className={styles.timeline}>
                  {STEPS.map((step) => {
                    const value = record[step.key];
                    return (
                      <li key={step.key} className={value ? styles.stepDone : styles.stepPending}>
                        <span className={styles.stepDot} />
                        <span className={styles.stepLabel}>{step.label}</span>
                        <span className={styles.stepTime}>{formatDate(value)}</span>
                      </li>
                    );
                  })}
                </ol>
              </>
            ) : null}

            <div className={styles.modalActions}>
              <button type="button" className={styles.secondary} onClick={() => setOpen(false)}>
                닫기
              </button>
            </div>
          </section>
        </div>
      )}
    </>
  );
}
