import type { RegistrationStatus } from "../types/clientRegistration";
import type { RegistrationRequest } from "../types/registrationRequest";
import { RequestStatusFilter } from "./RequestStatusFilter";
import { RequestTable } from "./RequestTable";
import styles from "./RegistrationRequestList.module.css";

interface RegistrationRequestListViewProps {
  status: RegistrationStatus | "";
  onStatusChange: (status: RegistrationStatus | "") => void;
  searchInput: string;
  onSearchInputChange: (value: string) => void;
  onSearchSubmit: () => void;
  items: RegistrationRequest[];
  loading: boolean;
  loadError: string;
  successMessage: string;
  onSelect: (request: RegistrationRequest) => void;
}

export function RegistrationRequestListView({
  status,
  onStatusChange,
  searchInput,
  onSearchInputChange,
  onSearchSubmit,
  items,
  loading,
  loadError,
  successMessage,
  onSelect,
}: RegistrationRequestListViewProps) {
  return (
    <main className={styles.shell}>
      <header className={styles.header}>
        <span className={styles.eyebrow}>CLIENT REGISTRATION</span>
        <h1>거래처 등록 신청 관리</h1>
        <p>거래처가 셀프 등록 신청한 내용을 검토하고 승인 또는 반려합니다.</p>
      </header>

      <form
        className={styles.filters}
        onSubmit={(event) => {
          event.preventDefault();
          onSearchSubmit();
        }}
      >
        <RequestStatusFilter value={status} onChange={onStatusChange} />
        <label className={`${styles.filterField} ${styles.searchField}`}>
          <span>검색어</span>
          <input
            value={searchInput}
            onChange={(event) => onSearchInputChange(event.target.value)}
            placeholder="회사명 · 사업자등록번호 · 신청자 이메일"
          />
        </label>
        <button type="submit" className={styles.searchButton}>
          검색
        </button>
      </form>

      {loadError && <p className={styles.error}>{loadError}</p>}
      {successMessage && <p className={styles.success}>{successMessage}</p>}

      <section className={styles.card}>
        <span className={styles.count}>{items.length}건</span>
        <RequestTable requests={items} loading={loading} onSelect={onSelect} />
      </section>
    </main>
  );
}