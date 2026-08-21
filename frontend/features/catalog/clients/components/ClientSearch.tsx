import styles from "./ClientList.module.css";

interface ClientSearchProps {
  keyword: string;
  onKeywordChange: (value: string) => void;
  isActive: boolean | null;
  onIsActiveChange: (value: boolean | null) => void;
  onSearchSubmit: () => void;
}

export function ClientSearch({ keyword, onKeywordChange, isActive, onIsActiveChange, onSearchSubmit }: ClientSearchProps) {
  return (
    <form
      className={styles.searchCard}
      onSubmit={(event) => {
        event.preventDefault();
        onSearchSubmit();
      }}
    >
      <div className={styles.filters}>
        <label className={styles.filterField}>
          <span>검색어</span>
          <input
            value={keyword}
            onChange={(event) => onKeywordChange(event.target.value)}
            placeholder="회사명 · 대표자 · 연락처 · 주소 · 담당자 통합검색"
          />
        </label>
        <label className={styles.filterField}>
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
      <div className={styles.filterActions}>
        <button type="submit" className={`${styles.btn} ${styles.btnPrimary}`}>
          조회
        </button>
      </div>
    </form>
  );
}