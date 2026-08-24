import { getCsrfToken } from "@/features/shared/api/csrf";
import type {
  ExcelUploadResult,
  ProductBulkUpsertRequest,
  ProductBulkUpsertResult,
  ProductFilters,
  ProductPage,
} from "../utils/productRow";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");
const BASE_PATH = "/api/admin/catalog/products";

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

function buildListQuery(filters: ProductFilters): string {
  const params = new URLSearchParams();
  if (filters.keyword) params.set("keyword", filters.keyword);
  if (filters.categoryId !== null) params.set("categoryId", String(filters.categoryId));
  if (filters.isActive !== null) params.set("isActive", String(filters.isActive));
  params.set("page", String(filters.page));
  params.set("size", String(filters.size));
  return params.toString();
}

function buildFilterQuery(filters: ProductFilters): string {
  const params = new URLSearchParams();
  if (filters.keyword) params.set("keyword", filters.keyword);
  if (filters.categoryId !== null) params.set("categoryId", String(filters.categoryId));
  if (filters.isActive !== null) params.set("isActive", String(filters.isActive));
  return params.toString();
}

async function downloadFile(path: string, filenameFallback: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: "include" });
  if (!response.ok) {
    throw new Error("파일을 내려받지 못했습니다.");
  }
  const disposition = response.headers.get("Content-Disposition") ?? "";
  const encoded = disposition.split("filename*=UTF-8''")[1]?.split(";")[0];
  const filename = encoded ? decodeURIComponent(encoded) : filenameFallback;

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}

export const catalogProductAdminService = {
  list(filters: ProductFilters) {
    return request<ProductPage>(`${BASE_PATH}?${buildListQuery(filters)}`);
  },

  upsertBulk(requests: ProductBulkUpsertRequest[]) {
    return request<ProductBulkUpsertResult[]>(`${BASE_PATH}/bulk`, {
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

  exportExcel(filters: ProductFilters) {
    return downloadFile(`${BASE_PATH}/excel/export?${buildFilterQuery(filters)}`, "품목목록.xlsx");
  },
};
