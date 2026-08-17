import { getCsrfToken } from "@/features/shared/api/csrf";
import type {
  AdminMember,
  AdminMemberPage,
  MemberFilters,
  UpdateRolesCommand,
} from "../types/member";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(
  /\/+$/,
  "",
);

interface ApiEnvelope<T> {
  success: boolean;
  message?: string;
  data?: T;
}

function isEnvelope<T>(value: unknown): value is ApiEnvelope<T> {
  return typeof value === "object" && value !== null && "success" in value;
}

export const memberService = {
  async getMembers(
    filters: MemberFilters,
    signal?: AbortSignal,
  ): Promise<AdminMemberPage> {
    const params = new URLSearchParams({
      query: filters.query,
      page: String(filters.page),
      size: String(filters.size),
    });

    const response = await fetch(
      `${API_BASE_URL}/api/admin/members?${params}`,
      {
        method: "GET",
        credentials: "include",
        cache: "no-store",
        signal,
      },
    );
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message =
        isEnvelope<AdminMemberPage>(body) && body.message
          ? body.message
          : `회원 목록을 조회하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<AdminMemberPage>(body) || !body.success || !body.data) {
      throw new Error("회원 목록 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },

  /** 관리자 전용 권한 변경. PUT /api/admin/members/{id}/roles — CSRF 보호 대상. */
  async updateRoles(
    command: UpdateRolesCommand,
    signal?: AbortSignal,
  ): Promise<AdminMember> {
    const csrf = await getCsrfToken(signal);
    const response = await fetch(
      `${API_BASE_URL}/api/admin/members/${command.id}/roles`,
      {
        method: "PUT",
        credentials: "include",
        cache: "no-store",
        signal,
        headers: {
          "Content-Type": "application/json",
          [csrf.headerName]: csrf.token,
        },
        body: JSON.stringify({ roles: command.roles }),
      },
    );
    const body: unknown = await response.json().catch(() => null);
    if (!response.ok) {
      const message =
        isEnvelope<AdminMember>(body) && body.message
          ? body.message
          : `권한을 변경하지 못했습니다. (${response.status})`;
      throw new Error(message);
    }
    if (!isEnvelope<AdminMember>(body) || !body.success || !body.data) {
      throw new Error("권한 변경 응답 형식이 올바르지 않습니다.");
    }
    return body.data;
  },
};
