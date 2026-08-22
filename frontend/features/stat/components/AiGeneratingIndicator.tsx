"use client";

import styles from "./AiGeneratingIndicator.module.css";

/** 버튼 라벨 옆에 붙이는 점 3개 애니메이션. currentColor를 써서 부모 텍스트 색을 그대로 물려받는다. */
export function AiGeneratingIndicator() {
  return (
    <span className={styles.dots} aria-hidden="true">
      <span className={styles.dot} />
      <span className={styles.dot} />
      <span className={styles.dot} />
    </span>
  );
}
