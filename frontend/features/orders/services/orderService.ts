import { getCsrfToken } from "@/features/shared/api/csrf";
import type {
  CreateOrderCommand,
  Order,
  OrderFilters,
  OrderPage,
  UpdateOrderCommand,
} from "../types/order";

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

export const orderService = {
  list(filters: OrderFilters = {}) {
    const params = new URLSearchParams();
    if (filters.status) params.set("status", filters.status);
    if (filters.q) params.set("q", filters.q);
    params.set("page", String(filters.page ?? 0));
    params.set("size", String(filters.size ?? 10));
    return request<OrderPage>(`/api/orders?${params.toString()}`);
  },

  listMine(filters: OrderFilters = {}) {
    const params = new URLSearchParams();
    if (filters.status) params.set("status", filters.status);
    if (filters.q) params.set("q", filters.q);
    params.set("page", String(filters.page ?? 0));
    params.set("size", String(filters.size ?? 10));
    return request<OrderPage>(`/api/orders/mine?${params.toString()}`);
  },

  get(id: string) {
    return request<Order>(`/api/orders/${id}`);
  },

  getMine(id: string) {
    return request<Order>(`/api/orders/mine/${id}`);
  },

  create(command: CreateOrderCommand) {
    return request<Order>("/api/orders", {
      method: "POST",
      body: JSON.stringify(command),
    });
  },

  update(id: string, command: UpdateOrderCommand) {
    return request<Order>(`/api/orders/${id}`, {
      method: "PUT",
      body: JSON.stringify(command),
    });
  },

  confirm(id: string) {
    return request<Order>(`/api/orders/${id}/confirm`, {
      method: "PATCH",
    });
  },

  cancel(id: string, reason: string) {
    return request<Order>(`/api/orders/${id}/cancel`, {
      method: "PATCH",
      body: JSON.stringify({ reason }),
    });
  },
};
