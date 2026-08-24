import { getCsrfToken } from "@/features/shared/api/csrf";
import type {
  AiSummaryLog,
  AiSummaryLogPage,
  DailyProduction,
  DashboardSummary,
  ProductDistribution,
  RagQaLog,
  RagQaLogPage,
  StatFilters,
} from "../types/stat";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(
  /\/+$/,
  "",
);

type ApiResponse<T> = {
  success: boolean;
  data: T;
  message?: string;
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const method = init?.method?.toUpperCase() ?? "GET";
  const headers = new Headers(init?.headers);

  if (method !== "GET" && method !== "HEAD") {
    const csrf = await getCsrfToken();
    headers.set("Content-Type", "application/json");
    headers.set(csrf.headerName, csrf.token);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
    credentials: "include",
  });
  const payload = (await response.json()) as ApiResponse<T>;

  if (!response.ok || !payload.success) {
    throw new Error(payload.message ?? "요청을 처리하지 못했습니다.");
  }

  return payload.data;
}

function toFilterParams(filters: StatFilters): URLSearchParams {
  const params = new URLSearchParams();
  if (filters.from) params.set("from", filters.from);
  if (filters.to) params.set("to", filters.to);
  if (filters.status) params.set("status", filters.status);
  return params;
}

export const statService = {
  dashboard(filters: StatFilters = {}) {
    return request<DashboardSummary>(
      `/api/admin/stats/dashboard?${toFilterParams(filters).toString()}`,
    );
  },

  dailyProduction(filters: StatFilters = {}) {
    return request<DailyProduction[]>(
      `/api/admin/stats/dashboard/daily?${toFilterParams(filters).toString()}`,
    );
  },

  products(filters: StatFilters = {}) {
    return request<ProductDistribution[]>(
      `/api/admin/stats/products?${toFilterParams(filters).toString()}`,
    );
  },

  summarize(filters: StatFilters = {}) {
    return request<AiSummaryLog>(
      `/api/admin/stats/summary?${toFilterParams(filters).toString()}`,
      {
        method: "POST",
      },
    );
  },

  summaryLogs(page = 0, size = 10) {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    return request<AiSummaryLogPage>(
      `/api/admin/stats/summary/logs?${params.toString()}`,
    );
  },

  ask(question: string) {
    return request<RagQaLog>(`/api/admin/stats/ask`, {
      method: "POST",
      body: JSON.stringify({ question }),
    });
  },

  askLogs(page = 0, size = 10) {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    return request<RagQaLogPage>(
      `/api/admin/stats/ask/logs?${params.toString()}`,
    );
  },
};
