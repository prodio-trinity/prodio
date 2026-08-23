"use client";

import { useState } from "react";
import { statService } from "../services/statService";
import type { RagQaLog } from "../types/stat";
import { useAiLogHistory } from "./useAiLogHistory";

/** 자유 질문을 받아 ask()를 호출하고, 최근 질의응답 로그를 보여준다. 대시보드 필터와는 무관하다. */
export function useRagQa() {
  const [question, setQuestion] = useState("");
  const [result, setResult] = useState<RagQaLog | null>(null);
  const [asking, setAsking] = useState(false);
  const [askError, setAskError] = useState("");

  const { logs, logsLoading, logsError, prepend } = useAiLogHistory(
    statService.askLogs,
    { errorFallback: "질의응답 로그를 불러오지 못했습니다." },
  );

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
      prepend(log);
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
