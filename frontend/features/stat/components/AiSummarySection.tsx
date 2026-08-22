"use client";

import { useAiSummary } from "../hooks/useAiSummary";
import type { StatFilters } from "../types/stat";
import styles from "./AiSummarySection.module.css";

interface AiSummarySectionProps {
  filters: StatFilters;
}

function formatDate(value: string) {
  return new Date(value).toLocaleString("ko-KR", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

export function AiSummarySection({ filters }: AiSummarySectionProps) {
  const {
    result,
    generating,
    generateError,
    generate,
    logs,
    logsLoading,
    logsError,
  } = useAiSummary(filters);

  return (
    <section className={styles.section}>
      <h3 className={styles.heading}>AI 요약</h3>

      <button
        type="button"
        onClick={() => void generate()}
        disabled={generating}
        className={styles.generateButton}
      >
        {generating ? "생성 중..." : "이 조건으로 요약 생성"}
      </button>

      {generateError ? <p className={styles.error}>{generateError}</p> : null}

      {result ? (
        <div className={styles.resultBox}>{result.response}</div>
      ) : null}

      <div>
        <p className={styles.subheading}>최근 요약 로그</p>
        {logsError ? <p className={styles.error}>{logsError}</p> : null}
        {!logsError && logsLoading ? (
          <p className={styles.placeholder}>불러오는 중...</p>
        ) : null}
        {!logsError && !logsLoading && logs.length === 0 ? (
          <p className={styles.placeholder}>아직 생성한 요약이 없습니다.</p>
        ) : null}
        {logs.length > 0 ? (
          <div className={styles.logList}>
            {logs.map((log) => (
              <div key={log.id} className={styles.logRow}>
                <span className={styles.logQuestion}>{log.question}</span>
                <span className={styles.logDate}>
                  {formatDate(log.requestedAt)}
                </span>
              </div>
            ))}
          </div>
        ) : null}
      </div>
    </section>
  );
}
