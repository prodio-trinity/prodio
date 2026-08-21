import gridStyles from "@/features/catalog/shared/components/AdminGrid.module.css";
import type { CatalogSubCategory, CatalogTopCategory } from "../utils/product";

interface ProductSearchProps {
  keyword: string;
  onKeywordChange: (value: string) => void;
  topCategories: CatalogTopCategory[];
  subCategories: CatalogSubCategory[];
  selectedTop: string;
  onTopChange: (code: string) => void;
  selectedSubId: string;
  onSubChange: (id: string) => void;
  isActive: boolean | null;
  onIsActiveChange: (value: boolean | null) => void;
  onReset: () => void;
  onSearchSubmit: () => void;
}

export function ProductSearch({
  keyword,
  onKeywordChange,
  topCategories,
  subCategories,
  selectedTop,
  onTopChange,
  selectedSubId,
  onSubChange,
  isActive,
  onIsActiveChange,
  onReset,
  onSearchSubmit,
}: ProductSearchProps) {
  return (
    <form
      className={gridStyles.searchCard}
      onSubmit={(event) => {
        event.preventDefault();
        onSearchSubmit();
      }}
    >
      <div className={gridStyles.filters} style={{ gridTemplateColumns: "2fr 1fr 1fr 1fr" }}>
        <label className={gridStyles.filterField}>
          <span>검색어</span>
          <input
            value={keyword}
            onChange={(event) => onKeywordChange(event.target.value)}
            placeholder="품목명 또는 품목코드"
          />
        </label>
        <label className={gridStyles.filterField}>
          <span>대분류</span>
          <select value={selectedTop} onChange={(event) => onTopChange(event.target.value)}>
            <option value="">전체</option>
            {topCategories.map((top) => (
              <option key={top.code} value={top.code}>
                {top.displayName}
              </option>
            ))}
          </select>
        </label>
        <label className={gridStyles.filterField}>
          <span>소분류</span>
          <select value={selectedSubId} onChange={(event) => onSubChange(event.target.value)}>
            <option value="">전체</option>
            {subCategories.map((sub) => (
              <option key={sub.id} value={sub.id}>
                {sub.name}
              </option>
            ))}
          </select>
        </label>
        <label className={gridStyles.filterField}>
          <span>사용여부</span>
          <select
            value={isActive === null ? "ALL" : isActive ? "ACTIVE" : "INACTIVE"}
            onChange={(event) => {
              const value = event.target.value;
              onIsActiveChange(value === "ALL" ? null : value === "ACTIVE");
            }}
          >
            <option value="ALL">전체</option>
            <option value="ACTIVE">사용</option>
            <option value="INACTIVE">미사용</option>
          </select>
        </label>
      </div>
      <div className={gridStyles.filterActions}>
        <button type="button" className={gridStyles.btn} onClick={onReset}>
          초기화
        </button>
        <button type="submit" className={`${gridStyles.btn} ${gridStyles.btnPrimary}`}>
          조회
        </button>
      </div>
    </form>
  );
}
