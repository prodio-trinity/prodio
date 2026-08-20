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
  const [memoTarget, setMemoTarget] = useState<ProductionRecord | null>(null);
  const [memoDraft, setMemoDraft] = useState("");
  const [memoSaving, setMemoSaving] = useState(false);
  const [memoError, setMemoError] = useState("");
  const [confirmingClear, setConfirmingClear] = useState(false);
  const [openMenuId, setOpenMenuId] = useState<string | null>(null);

  useEffect(() => {
    if (!openMenuId) return;
    function closeOnOutsideClick(event: MouseEvent) {
      const target = event.target as HTMLElement;
      if (!target.closest(`[data-menu-root]`)) setOpenMenuId(null);
    }
    window.addEventListener("mousedown", closeOnOutsideClick);
    return () => window.removeEventListener("mousedown", closeOnOutsideClick);
  }, [openMenuId]);

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

  function openMemo(record: ProductionRecord) {
    setMemoTarget(record);
    setMemoDraft("");
    setMemoError("");
    setConfirmingClear(false);
  }

  /** 메모 처리 후 목록을 다시 불러와서, 화면 테이블과 지금 열려있는 모달 둘 다 최신 값으로 맞춘다. */
  async function refreshAfterMemoChange() {
    const result = await productionService.list({ status: status || undefined, page, size: 20 });
    setData(result);
    setMemoTarget((current) => {
      if (!current) return current;
      return result.records.find((r) => r.id === current.id) ?? current;
    });
  }

  async function submitMemo() {
    if (!memoTarget || !memoDraft.trim()) return;
    setMemoSaving(true);
    setMemoError("");
    try {
      await productionService.addMemo(memoTarget.id, memoDraft.trim());
      setMemoDraft("");
      await refreshAfterMemoChange();
    } catch (cause) {
      setMemoError(cause instanceof Error ? cause.message : "메모를 추가하지 못했습니다.");
    } finally {
      setMemoSaving(false);
    }
  }

  async function submitClearMemo() {
    if (!memoTarget) return;
    setMemoSaving(true);
    setMemoError("");
    try {
      await productionService.clearMemo(memoTarget.id);
      setConfirmingClear(false);
      await refreshAfterMemoChange();
    } catch (cause) {
      setMemoError(cause instanceof Error ? cause.message : "메모를 비우지 못했습니다.");
    } finally {
      setMemoSaving(false);
    }
  }

  const memoEntries = (memoTarget?.memo ?? "").split("\n").filter((line) => line.trim() !== "");

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
                      <div className={styles.actionMenu} data-menu-root>
                        <button
                          type="button"
                          className={styles.menuTrigger}
                          aria-haspopup="true"
                          aria-expanded={openMenuId === record.id}
                          disabled={actingId === record.id}
                          onClick={() =>
                            setOpenMenuId((current) => (current === record.id ? null : record.id))
                          }
                        >
                          ⋮
                        </button>
                        {openMenuId === record.id && (
                          <div className={styles.menuDropdown} role="menu">
                            {record.status === "IN_PRODUCTION" && (
                              <button
                                type="button"
                                role="menuitem"
                                className={styles.menuItem}
                                onClick={() => {
                                  setPendingAction({ record, action: "ship" });
                                  setOpenMenuId(null);
                                }}
                              >
                                배송 시작
                              </button>
                            )}
                            {record.status === "IN_DELIVERY" && (
                              <button
                                type="button"
                                role="menuitem"
                                className={styles.menuItem}
                                onClick={() => {
                                  setPendingAction({ record, action: "complete" });
                                  setOpenMenuId(null);
                                }}
                              >
                                완료 처리
                              </button>
                            )}
                            <button
                              type="button"
                              role="menuitem"
                              className={styles.menuItem}
                              onClick={() => {
                                openMemo(record);
                                setOpenMenuId(null);
                              }}
                            >
                              메모
                            </button>
                          </div>
                        )}
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

      {memoTarget && (
        <div
          className={styles.modalBackdrop}
          role="presentation"
          onMouseDown={() => !memoSaving && setMemoTarget(null)}
        >
          <section
            className={styles.memoModal}
            role="dialog"
            aria-modal="true"
            aria-label="메모"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <div>
              <h2>메모</h2>
              <p>
                생산번호 #{memoTarget.id}(주문번호 #{memoTarget.orderId})
              </p>
            </div>

            <div className={styles.memoList}>
              {memoEntries.length === 0 ? (
                <p>등록된 메모가 없습니다.</p>
              ) : (
                memoEntries.map((entry, index) => (
                  <div key={index} className={styles.memoEntry}>
                    {entry}
                  </div>
                ))
              )}
            </div>

            {memoError && <p className={styles.error}>{memoError}</p>}

            <div className={styles.memoForm}>
              <textarea
                rows={3}
                maxLength={2000}
                value={memoDraft}
                disabled={memoSaving}
                onChange={(event) => setMemoDraft(event.target.value)}
                placeholder="메모를 입력하세요."
              />
              <div className={styles.memoModalFooter}>
                <button
                  type="button"
                  className={styles.primary}
                  disabled={memoSaving || !memoDraft.trim()}
                  onClick={() => void submitMemo()}
                >
                  추가
                </button>
              </div>
            </div>

            <div className={styles.memoModalActions}>
              {confirmingClear ? (
                <>
                  <span className={styles.dangerText}>정말 전체 메모를 삭제할까요?</span>
                  <div className={styles.memoModalFooter}>
                    <button type="button" className={styles.secondary} disabled={memoSaving}
                      onClick={() => setConfirmingClear(false)}>
                      취소
                    </button>
                    <button type="button" className={styles.primary} disabled={memoSaving}
                      onClick={() => void submitClearMemo()}>
                      삭제
                    </button>
                  </div>
                </>
              ) : (
                <>
                  <button
                    type="button"
                    className={styles.dangerText}
                    disabled={memoSaving || memoEntries.length === 0}
                    onClick={() => setConfirmingClear(true)}
                  >
                    전체 메모 삭제
                  </button>
                  <button type="button" className={styles.secondary} disabled={memoSaving}
                    onClick={() => setMemoTarget(null)}>
                    닫기
                  </button>
                </>
              )}
            </div>
          </section>
        </div>
      )}
    </main>
  );
}
