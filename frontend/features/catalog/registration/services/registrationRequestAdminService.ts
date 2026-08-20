import { getCsrfToken } from "@/features/shared/api/csrf";
import type { RegistrationStatus } from "../types/clientRegistration";
import type { ApproveResult, RegistrationRequest } from "../types/registrationRequest";

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

export const registrationRequestAdminService = {
  /** status가 빈 문자열이면 쿼리파라미터 자체를 생략 — 서버 기본값(전체 조회)에 맡긴다. */
  list(status: RegistrationStatus | "") {
    const query = status ? `?status=${status}` : "";
    return request<RegistrationRequest[]>(`/api/admin/catalog/clients/registration-requests${query}`);
  },

  approve(id: string) {
    return request<ApproveResult>(`/api/admin/catalog/clients/registration-requests/${id}/approve`, {
      method: "PATCH",
    });
  },

  reject(id: string, reason: string) {
    return request<void>(`/api/admin/catalog/clients/registration-requests/${id}/reject`, {
      method: "PATCH",
      body: JSON.stringify({ reason }),
    });
  },
};