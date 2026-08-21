import { categoryDisplayName, type CatalogProductSearchItem, type CatalogSubCategory, type CatalogTopCategory } from "../utils/product";
import styles from "./ProductPicker.module.css";

interface ProductSearchPanelProps {
  topCategories: CatalogTopCategory[];
  subCategories: CatalogSubCategory[];
  selectedTop: string;
  selectedSubId: string;
  onTopChange: (code: string) => void;
  onSubChange: (id: string) => void;

  keyword: string;
  onKeywordChange: (value: string) => void;
  onSubmit: () => void;

  results: CatalogProductSearchItem[];
  searching: boolean;
  dropdownOpen: boolean;
  onFocus: () => void;
  onSelect: (product: CatalogProductSearchItem) => void;
}

export function ProductSearchPanel({
  topCategories,
  subCategories,
  selectedTop,
  selectedSubId,
  onTopChange,
  onSubChange,
  keyword,
  onKeywordChange,
  onSubmit,
  results,
  searching,
  dropdownOpen,
  onFocus,
  onSelect,
}: ProductSearchPanelProps) {
  return (
    <>
      <div className={styles.categoryRow}>
        <select value={selectedTop} onChange={(event) => onTopChange(event.target.value)}>
          <option value="">대분류 전체</option>
          {topCategories.map((top) => (
            <option key={top.code} value={top.code}>
              {top.displayName}
            </option>
          ))}
        </select>
        <select value={selectedSubId} onChange={(event) => onSubChange(event.target.value)}>
          <option value="">소분류 전체</option>
          {subCategories.map((sub) => (
            <option key={sub.id} value={sub.id}>
              {sub.name}
            </option>
          ))}
        </select>
      </div>

      <div className={styles.searchRow}>
        <div className={styles.searchInputWrap}>
          <input
            value={keyword}
            onChange={(event) => onKeywordChange(event.target.value)}
            onFocus={onFocus}
            onKeyDown={(event) => {
              if (event.key === "Enter") {
                event.preventDefault();
                onSubmit();
              }
            }}
            placeholder="품목명 또는 품목코드로 검색"
          />
          {dropdownOpen ? (
            <div className={styles.dropdown}>
              {results.length > 0
                ? results.map((product) => (
                    <button
                      type="button"
                      key={product.id}
                      className={styles.resultCard}
                      onClick={() => onSelect(product)}
                    >
                      <div className={styles.resultHead}>
                        <span className={styles.resultName}>{product.productName}</span>
                        <span className={styles.resultBadge}>{categoryDisplayName(product)}</span>
                      </div>
                      <div className={styles.resultFoot}>
                        <span>{product.productCode}</span>
                        <span>{product.unitPrice.toLocaleString("ko-KR")}원</span>
                      </div>
                    </button>
                  ))
                : !searching && <p className={styles.emptyResult}>일치하는 품목이 없습니다.</p>}
            </div>
          ) : null}
        </div>
        <button type="button" onClick={onSubmit}>
          검색
        </button>
      </div>
    </>
  );
}