import type { CatalogCategoryList, CatalogProductSearchItem } from "../utils/product";

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/+$/, "");

type ApiResponse<T> = {
  success: boolean;
  data: T;
  message?: string;
};

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: "include" });
  const payload = (await response.json()) as ApiResponse<T>;

  if (!response.ok || !payload.success) {
    throw new Error(payload.message ?? "요청을 처리하지 못했습니다.");
  }

  return payload.data;
}

interface ProductSearchParams {
  keyword: string;
  /** 소분류(leaf) id */
  categoryId?: number;
  size?: number;
}

export const productSearchService = {
  async search({ keyword, categoryId, size = 8 }: ProductSearchParams): Promise<CatalogProductSearchItem[]> {
    const params = new URLSearchParams();
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (categoryId) params.set("categoryId", String(categoryId));
    params.set("size", String(size));

    const result = await request<{ products: CatalogProductSearchItem[] }>(`/api/catalog/products?${params}`);
    return result.products;
  },

  /** isActive 생략하면 비활성 포함 전체 조회 */
  getCategories(isActive?: boolean): Promise<CatalogCategoryList> {
    const query = isActive === undefined ? "" : `?isActive=${isActive}`;
    return request<CatalogCategoryList>(`/api/catalog/categories${query}`);
  },
};