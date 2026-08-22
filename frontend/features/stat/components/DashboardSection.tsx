"use client";

import { useStatDashboard } from "../hooks/useStatDashboard";
import {
  ORDER_VIEW_STATUS_LABELS,
  type OrderViewStatus,
  type StatFilters,
} from "../types/stat";
import styles from "./DashboardSection.module.css";

interface DashboardSectionProps {
  draft: StatFilters;
  onDraftChange: (next: StatFilters) => void;
  filters: StatFilters;
  onSubmit: () => void;
}

export function DashboardSection({
  draft,
  onDraftChange,
  filters,
  onSubmit,
}: DashboardSectionProps) {
  const { summary, distribution, loading, loadError } =
    useStatDashboard(filters);

  const maxOrderCount = Math.max(
    1,
    ...distribution.map((item) => item.orderCount),
  );

  return (
    <section className={styles.section}>
      <div className={styles.filterBar}>
        <input
          type="date"
          value={draft.from ?? ""}
          onChange={(event) =>
            onDraftChange({ ...draft, from: event.target.value || undefined })
          }
          className={styles.dateInput}
        />
        <span className={styles.filterSeparator}>~</span>
        <input
          type="date"
          value={draft.to ?? ""}
          onChange={(event) =>
            onDraftChange({ ...draft, to: event.target.value || undefined })
          }
          className={styles.dateInput}
        />
        <select
          value={draft.status ?? ""}
          onChange={(event) =>
            onDraftChange({
              ...draft,
              status: (event.target.value || undefined) as
                | OrderViewStatus
                | undefined,
            })
          }
          className={styles.statusSelect}
        >
          <option value="">전체 상태</option>
          {Object.entries(ORDER_VIEW_STATUS_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
        <button
          type="button"
          onClick={onSubmit}
          className={styles.submitButton}
        >
          조회
        </button>
      </div>

      {loadError ? <p className={styles.error}>{loadError}</p> : null}

      {loading && !summary ? (
        <p className={styles.placeholder}>불러오는 중...</p>
      ) : (
        <>
          <div className={styles.cardGrid}>
            {summary
              ? (
                  [
                    ["대기", summary.pendingCount],
                    ["생산중", summary.inProductionCount],
                    ["배송중", summary.inDeliveryCount],
                    ["완료", summary.completedCount],
                    ["취소", summary.cancelledCount],
                    ["전체", summary.totalCount],
                  ] as const
                ).map(([label, value]) => (
                  <div key={label} className={styles.card}>
                    <p className={styles.cardLabel}>{label}</p>
                    <p className={styles.cardValue}>{value}</p>
                  </div>
                ))
              : null}
            {summary ? (
              <div className={`${styles.card} ${styles.cardAccent}`}>
                <p className={styles.cardLabel}>생산량</p>
                <p className={styles.cardValue}>
                  {summary.completedQuantity.toLocaleString()}
                </p>
              </div>
            ) : null}
          </div>

          <div>
            <h3 className={styles.subheading}>품목별 분포</h3>
            {distribution.length === 0 ? (
              <p className={styles.placeholder}>
                조건에 맞는 품목 분포가 없습니다.
              </p>
            ) : (
              <table className={styles.distributionTable}>
                <tbody>
                  {distribution.map((item) => (
                    <tr key={item.productId}>
                      <td className={styles.distributionName}>
                        {item.productName}
                      </td>
                      <td className={styles.distributionCount}>
                        {item.orderCount}건
                      </td>
                      <td className={styles.distributionBarCell}>
                        <div className={styles.distributionBarTrack}>
                          <div
                            className={styles.distributionBarFill}
                            style={{
                              width: `${(item.orderCount / maxOrderCount) * 100}%`,
                            }}
                          />
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </section>
  );
}
