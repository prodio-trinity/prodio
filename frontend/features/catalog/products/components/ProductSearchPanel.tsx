import type { CatalogProductSearchItem, CatalogSubCategory, CatalogTopCategory } from "../utils/product";
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
        <input
          className={styles.searchInput}
          value={keyword}
          onChange={(event) => onKeywordChange(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter") {
              event.preventDefault();
              onSubmit();
            }
          }}
          placeholder="품목명 또는 품목코드로 검색"
        />
        <button type="button" onClick={onSubmit}>
          검색
        </button>
      </div>

      <div className={styles.resultsWrap}>
        {results.length === 0 ? (
          <p className={styles.emptyResult}>{searching ? "불러오는 중..." : "일치하는 품목이 없습니다."}</p>
        ) : (
          <table className={styles.resultsTable}>
            <thead>
              <tr>
                <th>품목코드</th>
                <th>품목명</th>
                <th>대분류</th>
                <th>소분류</th>
                <th>단가</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {results.map((product) => (
                <tr key={product.id} className={styles.resultRow} onClick={() => onSelect(product)}>
                  <td>{product.productCode}</td>
                  <td>{product.productName}</td>
                  <td>{product.topCategoryDisplayName ?? "-"}</td>
                  <td>{product.subCategoryName ?? "-"}</td>
                  <td>{product.unitPrice.toLocaleString("ko-KR")}원</td>
                  <td>
                    <button type="button" className={styles.selectBtn} onClick={() => onSelect(product)}>
                      선택
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}