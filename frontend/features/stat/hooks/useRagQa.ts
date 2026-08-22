"use client";

import { useCallback, useEffect, useState } from "react";
import { statService } from "../services/statService";
import type { RagQaLog } from "../types/stat";

const RECENT_LOGS_SIZE = 5;

/** 자유 질문을 받아 ask()를 호출하고, 최근 질의응답 로그를 보여준다. 대시보드 필터와는 무관하다. */
export function useRagQa() {
  const [question, setQuestion] = useState("");
  const [result, setResult] = useState<RagQaLog | null>(null);
  const [asking, setAsking] = useState(false);
  const [askError, setAskError] = useState("");

  const [logs, setLogs] = useState<RagQaLog[]>([]);
  const [logsLoading, setLogsLoading] = useState(true);
  const [logsError, setLogsError] = useState("");

  const loadLogs = useCallback(
    () =>
      statService
        .askLogs(0, RECENT_LOGS_SIZE)
        .then((page) => {
          setLogs(page.logs);
          setLogsError("");
        })
        .catch((cause: unknown) =>
          setLogsError(
            cause instanceof Error
              ? cause.message
              : "질의응답 로그를 불러오지 못했습니다.",
          ),
        )
        .finally(() => setLogsLoading(false)),
    [],
  );

  useEffect(() => {
    void loadLogs();
  }, [loadLogs]);

  async function ask() {
    const trimmed = question.trim();
    if (!trimmed) {
      setAskError("질문을 입력해주세요.");
      return;
    }

    setAsking(true);
    setAskError("");
    try {
      const log = await statService.ask(trimmed);
      setResult(log);
      setQuestion("");
      await loadLogs();
    } catch (cause) {
      setAskError(
        cause instanceof Error ? cause.message : "질의응답에 실패했습니다.",
      );
    } finally {
      setAsking(false);
    }
  }

  return {
    question,
    setQuestion,
    result,
    asking,
    askError,
    ask,
    logs,
    logsLoading,
    logsError,
  };
}
