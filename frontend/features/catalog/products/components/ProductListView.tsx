import Link from "next/link";
import { AdminGridFooter } from "@/features/catalog/shared/components/AdminGridFooter";
import { AdminGridHeader } from "@/features/catalog/shared/components/AdminGridHeader";
import gridStyles from "@/features/catalog/shared/components/AdminGrid.module.css";
import type { CatalogSubCategory, CatalogTopCategory } from "../utils/product";
import type { EditableProductRow } from "../utils/productRow";
import { ProductGridTable } from "./ProductGridTable";
import { ProductSearch } from "./ProductSearch";
import { SaveToast } from "./SaveToast";

interface ProductListViewProps {
  keyword: string;
  onKeywordChange: (value: string) => void;
  topCategories: CatalogTopCategory[];
  /** 검색 패널 소분류 select용 — 대분류 선택에 따라 좁혀진 소분류 목록 */
  filterSubCategories: CatalogSubCategory[];
  /** 그리드 분류 select용 — 검색 필터와 무관하게 항상 전체 소분류 목록  */
  allSubCategories: CatalogSubCategory[];
  selectedTop: string;
  onTopChange: (code: string) => void;
  selectedSubId: string;
  onSubChange: (id: string) => void;
  isActive: boolean | null;
  onIsActiveChange: (value: boolean | null) => void;
  onReset: () => void;
  onSearchSubmit: () => void;

  rows: EditableProductRow[];
  loading: boolean;
  loadError: string;
  onProductNameChange: (id: number, value: string) => void;
  onCategoryChange: (id: number, subCategoryId: number | null) => void;
  onUnitChange: (id: number, unit: string) => void;
  onUnitPriceChange: (id: number, unitPrice: number) => void;
  onMemoChange: (id: number, value: string) => void;
  onToggleActive: (id: number, checked: boolean) => void;
  onRemoveNewRow: (id: number) => void;

  totalElements: number;
  dirtyCount: number;
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;

  onAddRow: () => void;
  onSave: () => void;
  saving: boolean;
  saveError: string;
  saveMessage: string;
  toastId: number;

  exportError: string;
  onOpenUpload: () => void;
  onExport: () => void;
  exporting: boolean;
}

export function ProductListView({
  keyword,
  onKeywordChange,
  topCategories,
  filterSubCategories,
  allSubCategories,
  selectedTop,
  onTopChange,
  selectedSubId,
  onSubChange,
  isActive,
  onIsActiveChange,
  onReset,
  onSearchSubmit,
  rows,
  loading,
  loadError,
  onProductNameChange,
  onCategoryChange,
  onUnitChange,
  onUnitPriceChange,
  onMemoChange,
  onToggleActive,
  onRemoveNewRow,
  totalElements,
  dirtyCount,
  page,
  totalPages,
  onPageChange,
  onAddRow,
  onSave,
  saving,
  saveError,
  saveMessage,
  toastId,
  exportError,
  onOpenUpload,
  onExport,
  exporting,
}: ProductListViewProps) {
  return (
    <main className={gridStyles.shell}>
      <header className={gridStyles.header}>
        <span className={gridStyles.eyebrow}>PRODUCTS</span>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12 }}>
          <h1>품목 관리</h1>
          <Link
            href="/catalog/categories"
            className={`${gridStyles.btn} ${gridStyles.btnPrimary}`}
            style={{ transform: "scale(1.1)" }}
          >
            카테고리 관리
          </Link>
        </div>
        <p>품목을 조회하고 정보를 관리할 수 있습니다.</p>
      </header>

      <ProductSearch
        keyword={keyword}
        onKeywordChange={onKeywordChange}
        topCategories={topCategories}
        subCategories={filterSubCategories}
        selectedTop={selectedTop}
        onTopChange={onTopChange}
        selectedSubId={selectedSubId}
        onSubChange={onSubChange}
        isActive={isActive}
        onIsActiveChange={onIsActiveChange}
        onReset={onReset}
        onSearchSubmit={onSearchSubmit}
      />

      {loadError ? <p className={gridStyles.error}>{loadError}</p> : null}
      {saveError ? <p className={gridStyles.error}>{saveError}</p> : null}
      {exportError ? <p className={gridStyles.error}>{exportError}</p> : null}

      <section className={gridStyles.card}>
        <AdminGridHeader
          totalElements={totalElements}
          dirtyCount={dirtyCount}
          onAddRow={onAddRow}
          onSave={onSave}
          saving={saving}
        />

        <ProductGridTable
          rows={rows}
          loading={loading}
          topCategories={topCategories}
          subCategories={allSubCategories}
          onProductNameChange={onProductNameChange}
          onCategoryChange={onCategoryChange}
          onUnitChange={onUnitChange}
          onUnitPriceChange={onUnitPriceChange}
          onMemoChange={onMemoChange}
          onToggleActive={onToggleActive}
          onRemoveNewRow={onRemoveNewRow}
        />

        <AdminGridFooter
          page={page}
          totalPages={totalPages}
          onPageChange={onPageChange}
          onOpenUpload={onOpenUpload}
          onExport={onExport}
          exporting={exporting}
        />
      </section>

      <SaveToast message={saveMessage} toastId={toastId} />
    </main>
  );
}
