import { getCsrfToken } from "@/features/shared/api/csrf";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");
const BASE_PATH = "/api/admin/catalog/categories";

type ApiResponse<T> = {
  success: boolean;
  data: T;
  message?: string;
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const csrf = await getCsrfToken();
  const headers = new Headers(init?.headers);
  headers.set("Content-Type", "application/json");
  headers.set(csrf.headerName, csrf.token);

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

export const categoryAdminService = {
  create(parentCode: string, subCategoryCode: string, subCategoryName: string) {
    return request<void>(BASE_PATH, {
      method: "POST",
      body: JSON.stringify({ parentCode, subCategoryCode, subCategoryName }),
    });
  },

  /** subCategoryName/isActive는 항상 같이 보냄 — 부분 patch가 아니라 전체 상태 교체. */
  update(id: number, subCategoryName: string, isActive: boolean) {
    return request<void>(`${BASE_PATH}/${id}`, {
      method: "PUT",
      body: JSON.stringify({ subCategoryName, isActive }),
    });
  },
};