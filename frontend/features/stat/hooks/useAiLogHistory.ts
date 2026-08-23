"use client";

import { useCallback, useEffect, useState } from "react";

interface AiLogPage<T> {
  logs: T[];
}

interface UseAiLogHistoryOptions {
  size?: number;
  errorFallback: string;
}

/**
 * AI 요약/질의응답처럼 "최근 이력을 보여주고, 새로 생성된 결과를 바로 반영"하는 로그 목록의 공통 로직.
 * fetchPage는 statService.summaryLogs/askLogs처럼 안정적인 참조를 그대로 넘긴다(인라인 화살표 금지 —
 * 매 렌더마다 참조가 바뀌면 재조회 useEffect가 계속 다시 돈다).
 */
export function useAiLogHistory<T>(
  fetchPage: (page: number, size: number) => Promise<AiLogPage<T>>,
  { size = 5, errorFallback }: UseAiLogHistoryOptions,
) {
  const [logs, setLogs] = useState<T[]>([]);
  const [logsLoading, setLogsLoading] = useState(true);
  const [logsError, setLogsError] = useState("");

  const reload = useCallback(
    () =>
      fetchPage(0, size)
        .then((page) => {
          setLogs(page.logs);
          setLogsError("");
        })
        .catch((cause: unknown) =>
          setLogsError(cause instanceof Error ? cause.message : errorFallback),
        )
        .finally(() => setLogsLoading(false)),
    [fetchPage, size, errorFallback],
  );

  useEffect(() => {
    void reload();
  }, [reload]);

  /** 새로 생성된 로그를 재조회 없이 목록 맨 앞에 바로 반영한다. */
  const prepend = useCallback(
    (log: T) => setLogs((prev) => [log, ...prev].slice(0, size)),
    [size],
  );

  return { logs, logsLoading, logsError, reload, prepend };
}
