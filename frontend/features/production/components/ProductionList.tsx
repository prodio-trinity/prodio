"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { productionService } from "../services/productionService";
import {
  PRODUCTION_STATUS_LABELS,
  type ProductionPage,
  type ProductionRecord,
  type ProductionStatus,
} from "../types/production";
import styles from "./production.module.css";

const EMPTY: ProductionPage = { records: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };

const STATUS_TABS: { value: ProductionStatus | ""; label: string }[] = [
  { value: "", label: "전체 상태" },
  { value: "IN_PRODUCTION", label: "생산 중" },
  { value: "IN_DELIVERY", label: "배송 중" },
  { value: "COMPLETED", label: "완료" },
];

function formatDate(value: string | null) {
  if (!value) return "-";
  return new Date(value).toLocaleString("ko-KR", { dateStyle: "short", timeStyle: "short" });
}

type PendingAction = { record: ProductionRecord; action: "ship" | "complete" };

const ACTION_LABELS: Record<PendingAction["action"], string> = {
  ship: "배송 시작",
  complete: "완료 처리",
};

export function ProductionList() {
  const [data, setData] = useState(EMPTY);
  const [status, setStatus] = useState<ProductionStatus | "">("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actingId, setActingId] = useState<string | null>(null);
  const [actionError, setActionError] = useState("");
  const [pendingAction, setPendingAction] = useState<PendingAction | null>(null);

  const load = useCallback(
    () =>
      productionService
        .list({ status: status || undefined, page, size: 20 })
        .then((result) => {
          setData(result);
          setError("");
        })
        .catch((cause: unknown) =>
          setError(cause instanceof Error ? cause.message : "생산 목록을 불러오지 못했습니다."),
        )
        .finally(() => setLoading(false)),
    [status, page],
  );

  useEffect(() => {
    void load();
  }, [load]);

  async function confirmAction() {
    if (!pendingAction) return;
    const { record, action } = pendingAction;
    setActingId(record.id);
    setActionError("");
    try {
      const result = action === "ship"
        ? await productionService.ship(record.id)
        : await productionService.complete(record.id);
      if (!result.smsSent) {
        setActionError("처리는 완료됐지만 SMS 발송에는 실패했습니다.");
      }
      setPendingAction(null);
      await load();
    } catch (cause) {
      setActionError(cause instanceof Error ? cause.message : "처리하지 못했습니다.");
    } finally {
      setActingId(null);
    }
  }

  return (
    <main className={styles.shell}>
      <header className={styles.header}>
        <div>
          <span className={styles.eyebrow}>PRODUCTION</span>
          <h1>생산 관리</h1>
          <p>확정된 주문의 생산·배송 진행 상태를 관리합니다.</p>
        </div>
      </header>

      <section className={styles.card}>
        <div className={styles.tabs}>
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.value}
              type="button"
              className={`${styles.tab} ${status === tab.value ? styles.tabActive : ""}`}
              onClick={() => {
                setStatus(tab.value);
                setPage(0);
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {error && <p className={styles.error}>{error}</p>}
        {actionError && <p className={styles.error}>{actionError}</p>}

        {loading ? (
          <p className={styles.empty}>불러오는 중...</p>
        ) : data.records.length === 0 ? (
          <p className={styles.empty}>생산 기록이 없습니다.</p>
        ) : (
          <div className={styles.tableWrap}>
            <table>
              <thead>
                <tr>
                  <th>생산번호</th>
                  <th>주문번호</th>
                  <th>상태</th>
                  <th>연락처</th>
                  <th>시작</th>
                  <th>배송</th>
                  <th>완료</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {data.records.map((record) => (
                  <tr key={record.id}>
                    <td>#{record.id}</td>
                    <td>
                      <Link href={`/orders/${record.orderId}`}>#{record.orderId}</Link>
                    </td>
                    <td>
                      <span className={`${styles.badge} ${styles[`status${record.status}`]}`}>
                        {PRODUCTION_STATUS_LABELS[record.status]}
                      </span>
                    </td>
                    <td>{record.phone ?? "-"}</td>
                    <td>{formatDate(record.startedAt)}</td>
                    <td>{formatDate(record.shippedAt)}</td>
                    <td>{formatDate(record.completedAt)}</td>
                    <td>
                      <div className={styles.rowActions}>
                        {record.status === "IN_PRODUCTION" && (
                          <button
                            type="button"
                            className={styles.textButton}
                            disabled={actingId === record.id}
                            onClick={() => setPendingAction({ record, action: "ship" })}
                          >
                            배송 시작
                          </button>
                        )}
                        {record.status === "IN_DELIVERY" && (
                          <button
                            type="button"
                            className={styles.textButton}
                            disabled={actingId === record.id}
                            onClick={() => setPendingAction({ record, action: "complete" })}
                          >
                            완료 처리
                          </button>
                        )}
                        {record.status === "COMPLETED" && "-"}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className={styles.pagination}>
          <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>
            이전
          </button>
          <span>{data.totalPages === 0 ? 0 : page + 1} / {data.totalPages}</span>
          <button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>
            다음
          </button>
        </div>
      </section>

      {pendingAction && (
        <div
          className={styles.modalBackdrop}
          role="presentation"
          onMouseDown={() => actingId === null && setPendingAction(null)}
        >
          <section
            className={styles.confirmModal}
            role="dialog"
            aria-modal="true"
            aria-label="처리 확인"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <div>
              <h2>{ACTION_LABELS[pendingAction.action]}</h2>
              <p>
                생산번호 #{pendingAction.record.id}(주문번호 #{pendingAction.record.orderId})을(를){" "}
                {ACTION_LABELS[pendingAction.action]} 처리하시겠습니까?
              </p>
              <p>메세지가 발송됩니다.</p>
            </div>
            <div className={styles.confirmModalActions}>
              <button
                type="button"
                className={styles.secondary}
                disabled={actingId !== null}
                onClick={() => setPendingAction(null)}
              >
                취소
              </button>
              <button
                type="button"
                className={styles.primary}
                disabled={actingId !== null}
                onClick={() => void confirmAction()}
              >
                {ACTION_LABELS[pendingAction.action]}
              </button>
            </div>
          </section>
        </div>
      )}
    </main>
  );
}
