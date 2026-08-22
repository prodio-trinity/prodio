"use client";

import { Bot, ChevronDown, MessageCircle, X } from "lucide-react";
import { useState } from "react";
import { useRagQa } from "../hooks/useRagQa";
import { SOURCE_TYPE_LABELS } from "../types/stat";
import styles from "./RagQaSection.module.css";

type Tab = "ask" | "history";

function formatDate(value: string) {
  return new Date(value).toLocaleString("ko-KR", {
    dateStyle: "short",
    timeStyle: "short",
  });
}

export function RagQaSection() {
  const [open, setOpen] = useState(false);
  const [tab, setTab] = useState<Tab>("ask");
  const [expandedLogId, setExpandedLogId] = useState<string | null>(null);
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
    <>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className={styles.floatingButton}
        data-open={open}
        aria-label={open ? "AI 어시스턴트 닫기" : "AI 어시스턴트 열기"}
      >
        {open ? <X size={22} /> : <MessageCircle size={22} />}
        {open ? null : (
          <span className={styles.floatingButtonLabel}>AI에게 질문</span>
        )}
      </button>

      {open ? (
        <div className={styles.panel}>
          <div className={styles.panelHeader}>
            <div className={styles.panelHeaderTitle}>
              <span className={styles.panelIcon}>
                <Bot size={16} />
              </span>
              <h3 className={styles.heading}>AI 어시스턴트</h3>
            </div>
            <button
              type="button"
              onClick={() => setOpen(false)}
              className={styles.closeButton}
              aria-label="닫기"
            >
              <X size={16} />
            </button>
          </div>

          <div className={styles.tabs}>
            <button
              type="button"
              data-active={tab === "ask"}
              onClick={() => setTab("ask")}
              className={styles.tabButton}
            >
              질문
            </button>
            <button
              type="button"
              data-active={tab === "history"}
              onClick={() => setTab("history")}
              className={styles.tabButton}
            >
              이력
            </button>
          </div>

          {tab === "ask" ? (
            <div className={styles.tabContent}>
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
                <button
                  type="submit"
                  disabled={asking}
                  className={styles.askButton}
                >
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
              ) : (
                <p className={styles.placeholder}>
                  궁금한 걸 자유롭게 물어보세요.
                </p>
              )}
            </div>
          ) : (
            <div className={styles.tabContent}>
              {logsError ? <p className={styles.error}>{logsError}</p> : null}
              {!logsError && logsLoading ? (
                <p className={styles.placeholder}>불러오는 중...</p>
              ) : null}
              {!logsError && !logsLoading && logs.length === 0 ? (
                <p className={styles.placeholder}>
                  아직 질의응답 이력이 없습니다.
                </p>
              ) : null}
              {logs.length > 0 ? (
                <div className={styles.logList}>
                  {logs.map((log) => {
                    const isExpanded = expandedLogId === log.id;
                    return (
                      <div
                        key={log.id}
                        className={styles.logItem}
                        data-expanded={isExpanded}
                      >
                        <button
                          type="button"
                          className={styles.logRow}
                          onClick={() =>
                            setExpandedLogId(isExpanded ? null : log.id)
                          }
                          aria-expanded={isExpanded}
                        >
                          <div className={styles.logRowText}>
                            <span className={styles.logQuestion}>
                              {log.question}
                            </span>
                            <span className={styles.logDate}>
                              {formatDate(log.requestedAt)}
                            </span>
                          </div>
                          <ChevronDown
                            size={16}
                            className={styles.logChevron}
                          />
                        </button>
                        {isExpanded ? (
                          <div className={styles.logAnswer}>
                            {log.sourceType ? (
                              <span className={styles.badge}>
                                {SOURCE_TYPE_LABELS[log.sourceType]} 참고
                              </span>
                            ) : null}
                            <p className={styles.logAnswerText}>
                              {log.response}
                            </p>
                          </div>
                        ) : null}
                      </div>
                    );
                  })}
                </div>
              ) : null}
            </div>
          )}
        </div>
      ) : null}
    </>
  );
}
