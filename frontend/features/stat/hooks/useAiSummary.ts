"use client";

import { useState } from "react";
import { statService } from "../services/statService";
import type { AiSummaryLog, StatFilters } from "../types/stat";
import { useAiLogHistory } from "./useAiLogHistory";

/** filters(대시보드와 공유하는 조회 조건)로 AI 요약을 생성하고, 최근 요약 로그를 보여준다. */
export function useAiSummary(filters: StatFilters) {
  const [result, setResult] = useState<AiSummaryLog | null>(null);
  const [generating, setGenerating] = useState(false);
  const [generateError, setGenerateError] = useState("");
  // 조회 조건이 바뀌면 이전 조건으로 생성된 요약이 남아있지 않도록, 렌더 중에 바로 초기화한다
  // (useEffect로 하면 한 프레임 동안 이전 요약이 잠깐 보이는 깜빡임이 생긴다).
  const [resultFilters, setResultFilters] = useState(filters);
  if (resultFilters !== filters) {
    setResultFilters(filters);
    setResult(null);
  }

  const { logs, logsLoading, logsError, prepend } = useAiLogHistory(
    statService.summaryLogs,
    { errorFallback: "요약 로그를 불러오지 못했습니다." },
  );

  async function generate() {
    setGenerating(true);
    setGenerateError("");
    try {
      const log = await statService.summarize(filters);
      setResult(log);
      prepend(log);
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
