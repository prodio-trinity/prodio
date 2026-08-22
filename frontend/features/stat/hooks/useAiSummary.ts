"use client";

import { useCallback, useEffect, useState } from "react";
import { statService } from "../services/statService";
import type { AiSummaryLog, StatFilters } from "../types/stat";

const RECENT_LOGS_SIZE = 5;

/** filters(대시보드와 공유하는 조회 조건)로 AI 요약을 생성하고, 최근 요약 로그를 보여준다. */
export function useAiSummary(filters: StatFilters) {
  const [result, setResult] = useState<AiSummaryLog | null>(null);
  const [generating, setGenerating] = useState(false);
  const [generateError, setGenerateError] = useState("");

  const [logs, setLogs] = useState<AiSummaryLog[]>([]);
  const [logsLoading, setLogsLoading] = useState(true);
  const [logsError, setLogsError] = useState("");

  const loadLogs = useCallback(
    () =>
      statService
        .summaryLogs(0, RECENT_LOGS_SIZE)
        .then((page) => {
          setLogs(page.logs);
          setLogsError("");
        })
        .catch((cause: unknown) =>
          setLogsError(
            cause instanceof Error
              ? cause.message
              : "요약 로그를 불러오지 못했습니다.",
          ),
        )
        .finally(() => setLogsLoading(false)),
    [],
  );

  useEffect(() => {
    void loadLogs();
  }, [loadLogs]);

  async function generate() {
    setGenerating(true);
    setGenerateError("");
    try {
      const log = await statService.summarize(filters);
      setResult(log);
      await loadLogs();
    } catch (cause) {
      setGenerateError(
        cause instanceof Error ? cause.message : "요약 생성에 실패했습니다.",
      );
    } finally {
      setGenerating(false);
    }
  }

  return {
    result,
    generating,
    generateError,
    generate,
    logs,
    logsLoading,
    logsError,
  };
}
