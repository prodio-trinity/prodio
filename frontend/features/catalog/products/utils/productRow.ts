import type { CatalogSubCategory } from "./product";

const PAGE_SIZE = 20;

export interface ProductListItem {
  id: number;
  productCode: string;
  productName: string;
  subCategoryId: number | null;
  subCategoryName: string | null;
  topCategory: string | null;
  topCategoryDisplayName: string | null;
  unitPrice: number;
  unit: string;
  description: string | null;
  memo: string | null;
  isActive: boolean;
  createdAt: string;
}

export interface ProductPage {
  products: ProductListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** categoryId는 소분류 id, isActive는 null이면 전체 조회. */
export interface ProductFilters {
  keyword: string;
  categoryId: number | null;
  isActive: boolean | null;
  page: number;
  size: number;
}

export interface ProductBulkUpsertRequest {
  id: number | null;
  productName: string;
  categoryCode: string;
  unitPrice: number;
  unit: string;
  memo: string | null;
  isActive: boolean;
}

export interface ProductBulkUpsertResult {
  index: number;
  success: boolean;
  id: number | null;
  productCode: string | null;
  reason: string | null;
}

/**
 * 그리드 편집용 로컬 로우
 */
export interface EditableProductRow {
  id: number;
  productCode: string;
  productName: string;
  subCategoryId: number | null;
  unit: string;
  unitPrice: number;
  description: string;
  memo: string;
  isActive: boolean;
  createdAt: string;
  dirty: boolean;
  isNew: boolean;
  error: string | null;
}

export function initialFilters(): ProductFilters {
  return { keyword: "", categoryId: null, isActive: null, page: 0, size: PAGE_SIZE };
}

export function toEditableRow(item: ProductListItem): EditableProductRow {
  return {
    id: item.id,
    productCode: item.productCode,
    productName: item.productName,
    subCategoryId: item.subCategoryId,
    unit: item.unit,
    unitPrice: item.unitPrice,
    description: item.description ?? "",
    memo: item.memo ?? "",
    isActive: item.isActive,
    createdAt: item.createdAt,
    dirty: false,
    isNew: false,
    error: null,
  };
}

export function emptyEditableRow(tempId: number): EditableProductRow {
  return {
    id: tempId,
    productCode: "",
    productName: "",
    subCategoryId: null,
    unit: "EA",
    unitPrice: 0,
    description: "",
    memo: "",
    isActive: true,
    createdAt: "",
    dirty: false,
    isNew: true,
    error: null,
  };
}

/** row.subCategoryId(소분류 id)를 bulk 저장이 요구하는 categoryCode(소분류 코드)로 변환. */
export function toBulkUpsertRequest(row: EditableProductRow, subCategories: CatalogSubCategory[]): ProductBulkUpsertRequest {
  const category = subCategories.find((sub) => sub.id === row.subCategoryId);
  return {
    id: row.isNew ? null : row.id,
    productName: row.productName.trim(),
    categoryCode: category?.subCategoryCode ?? "",
    unitPrice: row.unitPrice,
    unit: row.unit,
    memo: row.memo.trim() || null,
    isActive: row.isActive,
  };
}

/**
 * 저장 후 재조회한 목록에 저장 실패한 행의 로컬 draft를 유지.
 * 기존 행은 사용자가 수정한 값을 유지하고, 신규 행은 재조회 결과에 없으므로 다시 추가.
 */
export function mergeFailedDrafts(
  rows: EditableProductRow[],
  failedDrafts: EditableProductRow[],
): EditableProductRow[] {
  const draftById = new Map(failedDrafts.map((row) => [row.id, row]));
  const merged = rows.map((row) => draftById.get(row.id) ?? row);
  const existingIds = new Set(rows.map((row) => row.id));
  const appended = failedDrafts.filter((row) => row.isNew && !existingIds.has(row.id));
  return [...merged, ...appended];
}
