"use client";

import { useEffect, useState } from "react";
import { statService } from "../services/statService";
import type {
  DailyProduction,
  DashboardSummary,
  ProductDistribution,
  StatFilters,
} from "../types/stat";

/** filters가 바뀔 때마다 대시보드 집계, 품목별 분포, 일별 생산량을 다시 조회한다. */
export function useStatDashboard(filters: StatFilters) {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [distribution, setDistribution] = useState<ProductDistribution[]>([]);
  const [daily, setDaily] = useState<DailyProduction[]>([]);
  const [loadError, setLoadError] = useState("");
  // 마지막으로 조회를 끝낸 filters. 현재 filters와 다르면 아직 로딩 중인 것으로 본다.
  const [loadedFilters, setLoadedFilters] = useState<StatFilters | null>(null);

  useEffect(() => {
    let cancelled = false;

    Promise.allSettled([
      statService.dashboard(filters),
      statService.products(filters),
      statService.dailyProduction(filters),
    ]).then(([summaryResult, distributionResult, dailyResult]) => {
      if (cancelled) return;

      if (summaryResult.status === "fulfilled") {
        setSummary(summaryResult.value);
      }
      if (distributionResult.status === "fulfilled") {
        setDistribution(distributionResult.value);
      }
      if (dailyResult.status === "fulfilled") {
        setDaily(dailyResult.value);
      }

      const failed = [summaryResult, distributionResult, dailyResult].find(
        (result) => result.status === "rejected",
      );
      setLoadError(
        failed
          ? failed.reason instanceof Error
            ? failed.reason.message
            : "통계를 불러오지 못했습니다."
          : "",
      );
      setLoadedFilters(filters);
    });

    return () => {
      cancelled = true;
    };
  }, [filters]);

  return {
    summary,
    distribution,
    daily,
    loading: loadedFilters !== filters,
    loadError,
  };
}
