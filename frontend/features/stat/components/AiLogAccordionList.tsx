"use client";

import { ChevronDown } from "lucide-react";
import { useState } from "react";
import styles from "./AiLogAccordionList.module.css";

interface AiLogEntry {
  id: string;
  question: string;
  response: string;
  requestedAt: string;
}

interface AiLogAccordionListProps<T extends AiLogEntry> {
  logs: T[];
  loading: boolean;
  error: string;
  emptyMessage: string;
  /** 답변 위에 배지로 보여줄 텍스트(예: 참고한 출처). 없으면 배지를 표시하지 않는다. */
  badgeText?: (log: T) => string | null | undefined;
}

function formatLogDate(value: string) {
  return new Date(value).toLocaleString("ko-KR", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

/** AI 요약/질의응답 이력 공용 아코디언 리스트. 행을 클릭하면 답변이 펼쳐진다. */
export function AiLogAccordionList<T extends AiLogEntry>({
  logs,
  loading,
  error,
  emptyMessage,
  badgeText,
}: AiLogAccordionListProps<T>) {
  const [expandedId, setExpandedId] = useState<string | null>(null);

  if (error) return <p className={styles.error}>{error}</p>;
  if (loading) return <p className={styles.placeholder}>불러오는 중...</p>;
  if (logs.length === 0) {
    return <p className={styles.placeholder}>{emptyMessage}</p>;
  }

  return (
    <div className={styles.list}>
      {logs.map((log) => {
        const isExpanded = expandedId === log.id;
        const badge = badgeText?.(log);
        return (
          <div key={log.id} className={styles.item} data-expanded={isExpanded}>
            <button
              type="button"
              className={styles.row}
              onClick={() => setExpandedId(isExpanded ? null : log.id)}
              aria-expanded={isExpanded}
            >
              <div className={styles.rowText}>
                <span className={styles.question}>{log.question}</span>
                <span className={styles.date}>
                  {formatLogDate(log.requestedAt)}
                </span>
              </div>
              <ChevronDown size={16} className={styles.chevron} />
            </button>
            {isExpanded ? (
              <div className={styles.answer}>
                {badge ? <span className={styles.badge}>{badge}</span> : null}
                <p className={styles.answerText}>{log.response}</p>
              </div>
            ) : null}
          </div>
        );
      })}
    </div>
  );
}
