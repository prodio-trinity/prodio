"use client";

import { useEffect, useState } from "react";
import { statService } from "../services/statService";
import type {
  DashboardSummary,
  ProductDistribution,
  StatFilters,
} from "../types/stat";

/** filters가 바뀔 때마다 대시보드 집계와 품목별 분포를 다시 조회한다. */
export function useStatDashboard(filters: StatFilters) {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [distribution, setDistribution] = useState<ProductDistribution[]>([]);
  const [loadError, setLoadError] = useState("");
  // 마지막으로 조회를 끝낸 filters. 현재 filters와 다르면 아직 로딩 중인 것으로 본다.
  const [loadedFilters, setLoadedFilters] = useState<StatFilters | null>(null);

  useEffect(() => {
    let cancelled = false;

    Promise.all([statService.dashboard(filters), statService.products(filters)])
      .then(([summaryResult, distributionResult]) => {
        if (cancelled) return;
        setSummary(summaryResult);
        setDistribution(distributionResult);
        setLoadError("");
      })
      .catch((cause: unknown) => {
        if (cancelled) return;
        setLoadError(
          cause instanceof Error
            ? cause.message
            : "통계를 불러오지 못했습니다.",
        );
      })
      .finally(() => {
        if (!cancelled) setLoadedFilters(filters);
      });

    return () => {
      cancelled = true;
    };
  }, [filters]);

  return {
    summary,
    distribution,
    loading: loadedFilters !== filters,
    loadError,
  };
}
