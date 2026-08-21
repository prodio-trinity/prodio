export interface CatalogTopCategory {
  code: string;
  displayName: string;
}

export interface CatalogSubCategory {
  id: number;
  subCategoryCode: string;
  name: string;
  topCategory: string;
  active: boolean;
}

export interface CatalogCategoryList {
  topCategories: CatalogTopCategory[];
  subCategories: CatalogSubCategory[];
}

/** GET /api/catalog/products 응답 아이템 */
export interface CatalogProductSearchItem {
  id: number;
  productCode: string;
  productName: string;
  subCategoryId: number | null;
  subCategoryName: string | null;
  topCategory: string | null;
  topCategoryDisplayName: string | null;
  unitPrice: number;
  unit: string;
}

/**
 * 검색 결과에서 선택하는 순간 스냅샷으로 굳혀서 담아두는 값.
 */
export interface SelectedProduct {
  productId: number;
  productCode: string;
  productName: string;
  categoryDisplayName: string;
  unit: string;
  unitPrice: number;
  quantity: number;
}

export function categoryDisplayName(item: CatalogProductSearchItem): string {
  return [item.topCategoryDisplayName, item.subCategoryName].filter(Boolean).join(" · ");
}

/** 이미 담긴 품목이면 수량만 +1, 아니면 검색 결과를 스냅샷으로 굳혀서 새 줄로 추가. */
export function addOrIncrementItem(items: SelectedProduct[], product: CatalogProductSearchItem): SelectedProduct[] {
  const existing = items.find((item) => item.productId === product.id);
  if (existing) {
    return items.map((item) => (item.productId === product.id ? { ...item, quantity: item.quantity + 1 } : item));
  }
  return [
    ...items,
    {
      productId: product.id,
      productCode: product.productCode,
      productName: product.productName,
      categoryDisplayName: categoryDisplayName(product),
      unit: product.unit,
      unitPrice: product.unitPrice,
      quantity: 1,
    },
  ];
}

export function updateItemQuantity(items: SelectedProduct[], productId: number, quantity: number): SelectedProduct[] {
  return items.map((item) => (item.productId === productId ? { ...item, quantity: Math.max(1, quantity || 1) } : item));
}

export function removeItem(items: SelectedProduct[], productId: number): SelectedProduct[] {
  return items.filter((item) => item.productId !== productId);
}