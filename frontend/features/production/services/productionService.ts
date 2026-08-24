import { getCsrfToken } from "@/features/shared/api/csrf";
import type {
  ProductionActionResult,
  ProductionFilters,
  ProductionPage,
  ProductionRecord,
} from "../types/production";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

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

export const productionService = {
  list(filters: ProductionFilters = {}) {
    const params = new URLSearchParams();
    if (filters.status) params.set("status", filters.status);
    params.set("page", String(filters.page ?? 0));
    params.set("size", String(filters.size ?? 20));
    return request<ProductionPage>(`/api/admin/production?${params.toString()}`);
  },

  ship(id: string) {
    return request<ProductionActionResult>(`/api/admin/production/${id}/ship`, {
      method: "PATCH",
    });
  },

  complete(id: string) {
    return request<ProductionActionResult>(`/api/admin/production/${id}/complete`, {
      method: "PATCH",
    });
  },

  addMemo(id: string, memo: string) {
    return request<void>(`/api/admin/production/${id}/memo`, {
      method: "PATCH",
      body: JSON.stringify({ memo }),
    });
  },

  clearMemo(id: string) {
    return request<void>(`/api/admin/production/${id}/memo`, {
      method: "DELETE",
    });
  },

  /** 고객 화면(내 수주 현황 등)에서 주문번호로 배송 현황을 조회한다. 생산 레코드가 아직 없으면(주문 미확정) 404. */
  getByOrderId(orderId: string) {
    return request<ProductionRecord>(`/api/production/${orderId}`);
  },
};
