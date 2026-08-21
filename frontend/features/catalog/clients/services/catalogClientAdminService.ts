import { getCsrfToken } from "@/features/shared/api/csrf";
import type {
  ClientBulkUpsertRequest,
  ClientBulkUpsertResult,
  ClientFilters,
  ClientPage,
  ExcelUploadResult,
} from "../types/client";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");
const BASE_PATH = "/api/admin/catalog/clients";

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
    headers.set(csrf.headerName, csrf.token);
    if (!(init?.body instanceof FormData)) {
      headers.set("Content-Type", "application/json");
    }
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

function buildListQuery(filters: ClientFilters): string {
  const params = new URLSearchParams();
  if (filters.keyword) params.set("keyword", filters.keyword);
  if (filters.isActive !== null) params.set("isActive", String(filters.isActive));
  params.set("page", String(filters.page));
  params.set("size", String(filters.size));
  return params.toString();
}

async function downloadFile(path: string, filenameFallback: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: "include" });
  if (!response.ok) {
    throw new Error("파일을 내려받지 못했습니다.");
  }
  const disposition = response.headers.get("Content-Disposition") ?? "";
  const match = disposition.match(/filename="?([^";]+)"?/);
  const filename = match ? decodeURIComponent(match[1]) : filenameFallback;

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}

export const catalogClientAdminService = {
  list(filters: ClientFilters) {
    return request<ClientPage>(`${BASE_PATH}?${buildListQuery(filters)}`);
  },

  upsertBulk(requests: ClientBulkUpsertRequest[]) {
    return request<ClientBulkUpsertResult[]>(`${BASE_PATH}/bulk`, {
      method: "POST",
      body: JSON.stringify(requests),
    });
  },

  async uploadExcel(file: File) {
    const formData = new FormData();
    formData.set("file", file);
    return request<ExcelUploadResult>(`${BASE_PATH}/excel/upload`, {
      method: "POST",
      body: formData,
    });
  },

  exportExcel(filters: ClientFilters) {
    const params = new URLSearchParams();
    if (filters.keyword) params.set("keyword", filters.keyword);
    if (filters.isActive !== null) params.set("isActive", String(filters.isActive));
    return downloadFile(`${BASE_PATH}/excel/export?${params.toString()}`, "거래처목록.xlsx");
  },
};