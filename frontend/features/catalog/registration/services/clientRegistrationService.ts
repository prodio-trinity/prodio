import { getCsrfToken } from "@/features/shared/api/csrf";
import type { MyRegistration, RegistrationSubmitRequest } from "../types/clientRegistration";

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

export const clientRegistrationService = {
  getMy() {
    return request<MyRegistration>("/api/catalog/clients/me");
  },

  submit(command: RegistrationSubmitRequest) {
    return request<MyRegistration>("/api/catalog/clients/me/registration-requests", {
      method: "POST",
      body: JSON.stringify(command),
    });
  },
};