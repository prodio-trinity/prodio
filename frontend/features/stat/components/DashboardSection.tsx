"use client";

import { useState } from "react";
import type { useAiSummary } from "../hooks/useAiSummary";
import { useStatDashboard } from "../hooks/useStatDashboard";
import {
  ORDER_VIEW_STATUS_LABELS,
  type OrderViewStatus,
  type StatFilters,
} from "../types/stat";
import {
  lastMonthRange,
  thisMonthRange,
  thisWeekRange,
} from "../utils/dateRanges";
import { AiGeneratingIndicator } from "./AiGeneratingIndicator";
import { AiSummarySkeleton } from "./AiSummarySkeleton";
import styles from "./DashboardSection.module.css";

interface DashboardSectionProps {
  draft: StatFilters;
  onDraftChange: (next: StatFilters) => void;
  filters: StatFilters;
  onSubmit: () => void;
  onApplyPreset: (range: StatFilters) => void;
  aiSummary: ReturnType<typeof useAiSummary>;
}

const TOP_N = 5;
const STATUS_ORDER: OrderViewStatus[] = [
  "PENDING",
  "IN_PRODUCTION",
  "IN_DELIVERY",
  "COMPLETED",
  "CANCELLED",
];

const PRESETS = [
  { label: "이번주", range: thisWeekRange },
  { label: "이번달", range: thisMonthRange },
  { label: "저번달", range: lastMonthRange },
] as const;

function formatShortDate(value: string) {
  const [, month, day] = value.split("-");
  return `${month}.${day}`;
}

export function DashboardSection({
  draft,
  onDraftChange,
  filters,
  onSubmit,
  onApplyPreset,
  aiSummary,
}: DashboardSectionProps) {
  const { summary, distribution, daily, loading, loadError } =
    useStatDashboard(filters);
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);
  const aiPreviewText =
    aiSummary.result?.response ?? aiSummary.logs[0]?.response ?? null;

  const topDistribution = [...distribution]
    .sort((a, b) => b.orderCount - a.orderCount)
    .slice(0, TOP_N);
  const maxDistributionCount = Math.max(
    1,
    ...topDistribution.map((item) => item.orderCount),
  );
  const maxDailyQuantity = Math.max(1, ...daily.map((item) => item.quantity));

  const chartPoints = daily.map((item, index) => ({
    left: daily.length === 1 ? 50 : (index / (daily.length - 1)) * 100,
    top: 90 - (item.quantity / maxDailyQuantity) * 80,
  }));
  const linePath = chartPoints
    .map(
      (point, index) => `${index === 0 ? "M" : "L"}${point.left},${point.top}`,
    )
    .join(" ");
  const areaPath =
    chartPoints.length > 1
      ? `${linePath} L${chartPoints[chartPoints.length - 1].left},100 L${chartPoints[0].left},100 Z`
      : "";

  const totalCount = summary?.totalCount ?? 0;
  const statusCounts: Record<OrderViewStatus, number> = summary
    ? {
        PENDING: summary.pendingCount,
        IN_PRODUCTION: summary.inProductionCount,
        IN_DELIVERY: summary.inDeliveryCount,
        COMPLETED: summary.completedCount,
        CANCELLED: summary.cancelledCount,
      }
    : {
        PENDING: 0,
        IN_PRODUCTION: 0,
        IN_DELIVERY: 0,
        COMPLETED: 0,
        CANCELLED: 0,
      };

  return (
    <>
      <header className={styles.header}>
        <span className={styles.eyebrow}>STATISTICS</span>
        <h1>통계 대시보드</h1>
        <p>기간별 주문 현황과 생산 통계를 확인합니다.</p>
      </header>

      <section className={styles.section}>
        <div className={styles.controls}>
          <div className={styles.dateRange}>
            <input
              type="date"
              value={draft.from ?? ""}
              onChange={(event) =>
                onDraftChange({
                  ...draft,
                  from: event.target.value || undefined,
                })
              }
              className={styles.dateInput}
            />
            <span className={styles.dateArrow}>→</span>
            <input
              type="date"
              value={draft.to ?? ""}
              onChange={(event) =>
                onDraftChange({ ...draft, to: event.target.value || undefined })
              }
              className={styles.dateInput}
            />
          </div>

          <button
            type="button"
            onClick={onSubmit}
            className={styles.submitButton}
          >
            조회
          </button>

          <span className={styles.controlsDivider} />

          <div className={styles.presets}>
            {PRESETS.map(({ label, range }) => {
              const computed = range();
              const isActive =
                draft.from === computed.from && draft.to === computed.to;
              return (
                <button
                  key={label}
                  type="button"
                  data-active={isActive}
                  onClick={() => onApplyPreset(computed)}
                  className={styles.presetButton}
                >
                  {label}
                </button>
              );
            })}
          </div>
        </div>

        <hr className={styles.divider} />

        {loadError ? <p className={styles.error}>{loadError}</p> : null}

        {loading && !summary ? (
          <p className={styles.placeholder}>불러오는 중...</p>
        ) : (
          <div className={styles.body}>
            <div className={styles.column}>
              <p className={styles.metricLabel}>기간 내 총 생산량</p>
              <p className={styles.metricValue}>
                {(summary?.completedQuantity ?? 0).toLocaleString()}
                <span className={styles.metricUnit}>EA</span>
              </p>

              {daily.length > 0 ? (
                <>
                  <div
                    className={styles.chart}
                    onMouseLeave={() => setHoveredIndex(null)}
                  >
                    {chartPoints.length > 1 ? (
                      <svg
                        viewBox="0 0 100 100"
                        preserveAspectRatio="none"
                        className={styles.chartSvg}
                      >
                        <defs>
                          <linearGradient
                            id="dailyProductionArea"
                            x1="0"
                            y1="0"
                            x2="0"
                            y2="1"
                          >
                            <stop
                              offset="0%"
                              stopColor="var(--color-primary)"
                              stopOpacity="0.25"
                            />
                            <stop
                              offset="100%"
                              stopColor="var(--color-primary)"
                              stopOpacity="0"
                            />
                          </linearGradient>
                        </defs>
                        <path d={areaPath} className={styles.chartArea} />
                        <path
                          d={linePath}
                          className={styles.chartLine}
                          vectorEffect="non-scaling-stroke"
                        />
                      </svg>
                    ) : null}

                    <div className={styles.chartHoverLayer}>
                      {daily.map((item, index) => (
                        <div
                          key={item.date}
                          className={styles.chartHoverZone}
                          onMouseEnter={() => setHoveredIndex(index)}
                        >
                          {hoveredIndex === index ? (
                            <div className={styles.chartTooltip}>
                              {formatShortDate(item.date)} ·{" "}
                              {item.quantity.toLocaleString()} EA
                            </div>
                          ) : null}
                        </div>
                      ))}
                    </div>

                    {chartPoints.map((point, index) => (
                      <div
                        key={daily[index].date}
                        className={styles.chartDot}
                        data-active={hoveredIndex === index}
                        style={{ left: `${point.left}%`, top: `${point.top}%` }}
                      />
                    ))}
                  </div>
                  <div className={styles.sparklineAxis}>
                    <span>{formatShortDate(daily[0].date)}</span>
                    <span>{formatShortDate(daily[daily.length - 1].date)}</span>
                  </div>
                </>
              ) : null}

              <div className={styles.metricRow}>
                <span className={styles.metricRowLabel}>전체 주문</span>
                <span className={styles.metricRowValue}>{totalCount}건</span>
              </div>

              <div className={styles.aiCompact}>
                <div className={styles.aiCompactHeader}>
                  <span className={styles.aiCompactLabel}>AI 요약</span>
                  <button
                    type="button"
                    onClick={() => void aiSummary.generate()}
                    disabled={aiSummary.generating}
                    className={styles.aiCompactButton}
                  >
                    {aiSummary.generating ? (
                      <>
                        생성 중<AiGeneratingIndicator />
                      </>
                    ) : aiPreviewText ? (
                      "다시 생성"
                    ) : (
                      "생성"
                    )}
                  </button>
                </div>
                {aiSummary.generating ? (
                  <AiSummarySkeleton lines={2} />
                ) : aiPreviewText ? (
                  <p className={styles.aiCompactText}>{aiPreviewText}</p>
                ) : (
                  <p className={styles.placeholder}>
                    이 조건으로 AI 요약을 생성해보세요.
                  </p>
                )}
              </div>
            </div>

            <div className={styles.column}>
              <h3 className={styles.subheading}>상태별 현황</h3>

              {totalCount === 0 ? (
                <div className={styles.stackedBar}>
                  <div className={styles.stackedEmpty} />
                </div>
              ) : (
                <div className={styles.stackedBar}>
                  {STATUS_ORDER.filter(
                    (status) => statusCounts[status] > 0,
                  ).map((status) => (
                    <div
                      key={status}
                      className={styles.stackedSegment}
                      data-status={status}
                      style={{ flexGrow: statusCounts[status] }}
                    />
                  ))}
                </div>
              )}

              <div className={styles.statusList}>
                {STATUS_ORDER.map((status) => (
                  <div key={status} className={styles.statusRow}>
                    <span
                      className={styles.statusBullet}
                      data-status={status}
                    />
                    <span className={styles.statusLabel}>
                      {ORDER_VIEW_STATUS_LABELS[status]}
                    </span>
                    <span className={styles.statusCount}>
                      {statusCounts[status]}
                    </span>
                    <span className={styles.statusPercent}>
                      {totalCount === 0
                        ? 0
                        : Math.round((statusCounts[status] / totalCount) * 100)}
                      %
                    </span>
                  </div>
                ))}
              </div>

              <div className={styles.distributionHeader}>
                <h3 className={styles.subheading}>품목별 분포</h3>
                {topDistribution.length > 0 ? (
                  <span className={styles.topTag}>
                    TOP {topDistribution.length}
                  </span>
                ) : null}
              </div>
              {topDistribution.length === 0 ? (
                <p className={styles.placeholder}>
                  조건에 맞는 품목 분포가 없습니다.
                </p>
              ) : (
                <div className={styles.distributionList}>
                  {topDistribution.map((item, index) => (
                    <div
                      key={item.productId}
                      className={styles.distributionRow}
                    >
                      <span className={styles.distributionName}>
                        {item.productName}
                      </span>
                      <div className={styles.distributionBarTrack}>
                        <div
                          className={styles.distributionBarFill}
                          style={{
                            width: `${(item.orderCount / maxDistributionCount) * 100}%`,
                            opacity: 1 - index * 0.15,
                          }}
                        />
                      </div>
                      <span className={styles.distributionCount}>
                        {item.orderCount}건
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </section>
    </>
  );
}
