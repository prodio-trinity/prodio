import { getCsrfToken } from "@/features/shared/api/csrf";
import { CurrentUser, MeResponse } from "../types/auth";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

function buildApiUrl(path: string): string {
  return `${API_BASE_URL}${path}`;
}

function isMeResponse(value: unknown): value is MeResponse {
  if (typeof value !== "object" || value === null) return false;

  const response = value as Record<string, unknown>;
  return (
    typeof response.id === "string" &&
    typeof response.name === "string" &&
    typeof response.email === "string" &&
    Array.isArray(response.roles) &&
    response.roles.every((role) => typeof role === "string")
  );
}

function isCurrentUser(value: unknown): value is CurrentUser {
  return isMeResponse(value);
}

interface ApiEnvelope {
  success: boolean;
  message?: string;
  errorCode?: string;
  data?: unknown;
}

function isApiEnvelope(value: unknown): value is ApiEnvelope {
  return typeof value === "object" && value !== null && "success" in value;
}

/** CSRF 보호 대상(POST/PUT/PATCH/DELETE) 요청 공통 처리. 실패 시 서버가 내려준 메시지로 에러를 던진다. */
async function csrfProtectedRequest(path: string, init: RequestInit, signal?: AbortSignal): Promise<unknown> {
  const csrf = await getCsrfToken(signal);
  const response = await fetch(buildApiUrl(path), {
    ...init,
    credentials: "include",
    cache: "no-store",
    signal,
    headers: {
      "Content-Type": "application/json",
      [csrf.headerName]: csrf.token,
      ...init.headers,
    },
  });

  const body: unknown = await response.json().catch(() => null);
  if (!response.ok) {
    const message = isApiEnvelope(body) && body.message ? body.message : `요청에 실패했습니다. (${response.status})`;
    throw new Error(message);
  }
  return isApiEnvelope(body) ? body.data : body;
}

export const authService = {
  async getCurrentUser(signal?: AbortSignal): Promise<CurrentUser | null> {
    const response = await fetch(buildApiUrl("/api/me"), {
      method: "GET",
      credentials: "include",
      cache: "no-store",
      signal,
    });

    if (response.status === 401) return null;

    if (!response.ok) {
      throw new Error(`인증 확인에 실패했습니다. (${response.status})`);
    }

    const body: unknown = await response.json();
    if (!isMeResponse(body)) {
      throw new Error("인증 서버 응답 형식이 올바르지 않습니다.");
    }

    return body;
  },

  async login(email: string, password: string, signal?: AbortSignal): Promise<CurrentUser> {
    const data = await csrfProtectedRequest(
      "/auth/login",
      { method: "POST", body: JSON.stringify({ email, password }) },
      signal,
    );
    if (!isCurrentUser(data)) throw new Error("로그인 응답 형식이 올바르지 않습니다.");
    return data;
  },

  async signup(email: string, password: string, name: string, signal?: AbortSignal): Promise<CurrentUser> {
    const data = await csrfProtectedRequest(
      "/auth/signup",
      { method: "POST", body: JSON.stringify({ email, password, name }) },
      signal,
    );
    if (!isCurrentUser(data)) throw new Error("회원가입 응답 형식이 올바르지 않습니다.");
    return data;
  },

  async logout(signal?: AbortSignal): Promise<void> {
    await csrfProtectedRequest("/auth/logout", { method: "POST" }, signal);
  },
};
