"use client";

import styles from "./AiSummarySkeleton.module.css";

interface AiSummarySkeletonProps {
  lines?: 2 | 3;
}

const WIDTHS_BY_LINE_COUNT: Record<2 | 3, string[]> = {
  2: ["88%", "60%"],
  3: ["92%", "78%", "55%"],
};

/** AI가 요약을 쓰고 있는 느낌을 주는 반짝이는 스켈레톤 라인. */
export function AiSummarySkeleton({ lines = 3 }: AiSummarySkeletonProps) {
  return (
    <div className={styles.skeleton} aria-hidden="true">
      {WIDTHS_BY_LINE_COUNT[lines].map((width, index) => (
        <div key={index} className={styles.line} style={{ width }} />
      ))}
    </div>
  );
}
