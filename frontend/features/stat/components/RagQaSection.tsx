"use client";

import { useRagQa } from "../hooks/useRagQa";
import { SOURCE_TYPE_LABELS } from "../types/stat";
import styles from "./RagQaSection.module.css";

function formatDate(value: string) {
  return new Date(value).toLocaleString("ko-KR", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

export function RagQaSection() {
  const {
    question,
    setQuestion,
    result,
    asking,
    askError,
    ask,
    logs,
    logsLoading,
    logsError,
  } = useRagQa();

  return (
    <section className={styles.section}>
      <h3 className={styles.heading}>무엇이든 물어보세요</h3>

      <form
        className={styles.askForm}
        onSubmit={(event) => {
          event.preventDefault();
          void ask();
        }}
      >
        <input
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="예: 지난달 매출이랑 배송 지연 이슈 같이 알려줘"
          className={styles.questionInput}
          disabled={asking}
        />
        <button type="submit" disabled={asking} className={styles.askButton}>
          {asking ? "답변 생성 중..." : "질문"}
        </button>
      </form>

      {askError ? <p className={styles.error}>{askError}</p> : null}

      {result ? (
        <div className={styles.resultBox}>
          {result.sourceType ? (
            <span className={styles.badge}>
              {SOURCE_TYPE_LABELS[result.sourceType]} 참고
            </span>
          ) : null}
          <p className={styles.resultText}>{result.response}</p>
        </div>
      ) : null}

      <div>
        <p className={styles.subheading}>최근 질의응답 로그</p>
        {logsError ? <p className={styles.error}>{logsError}</p> : null}
        {!logsError && logsLoading ? (
          <p className={styles.placeholder}>불러오는 중...</p>
        ) : null}
        {!logsError && !logsLoading && logs.length === 0 ? (
          <p className={styles.placeholder}>아직 질의응답 이력이 없습니다.</p>
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
