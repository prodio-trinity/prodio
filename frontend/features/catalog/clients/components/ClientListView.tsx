import { Building2 } from "lucide-react";
import type { EditableClientRow } from "../types/client";
import type { EditableTextField } from "../utils/clientRow";
import { ClientGridTable } from "./ClientGridTable";
import { ClientListFooter } from "./ClientListFooter";
import { ClientListHeader } from "./ClientListHeader";
import { ClientSearch } from "./ClientSearch";
import { SaveToast } from "./SaveToast";
import styles from "./ClientList.module.css";

interface ClientListViewProps {
  keyword: string;
  onKeywordChange: (value: string) => void;
  isActive: boolean | null;
  onIsActiveChange: (value: boolean | null) => void;
  onSearchSubmit: () => void;

  rows: EditableClientRow[];
  loading: boolean;
  loadError: string;
  onCellChange: (id: number, field: EditableTextField, value: string) => void;
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
  saveMessage: string;
  toastId: number;

  exportError: string;
  onOpenUpload: () => void;
  onExport: () => void;
  exporting: boolean;
}

export function ClientListView({
  keyword,
  onKeywordChange,
  isActive,
  onIsActiveChange,
  onSearchSubmit,
  rows,
  loading,
  loadError,
  onCellChange,
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
  saveMessage,
  toastId,
  exportError,
  onOpenUpload,
  onExport,
  exporting,
}: ClientListViewProps) {
  return (
    <main className={styles.shell}>
      <header className={styles.header}>
        <span className={styles.eyebrow}>
          <Building2 size={14} /> CLIENTS
        </span>
        <h1>거래처 관리</h1>
        <p>거래처 정보를 조회하고 그리드에서 바로 수정합니다.</p>
      </header>

      <ClientSearch
        keyword={keyword}
        onKeywordChange={onKeywordChange}
        isActive={isActive}
        onIsActiveChange={onIsActiveChange}
        onSearchSubmit={onSearchSubmit}
      />

      {loadError ? <p className={styles.error}>{loadError}</p> : null}
      {exportError ? <p className={styles.error}>{exportError}</p> : null}

      <section className={styles.card}>
        <ClientListHeader
          totalElements={totalElements}
          dirtyCount={dirtyCount}
          onAddRow={onAddRow}
          onSave={onSave}
          saving={saving}
        />

        <ClientGridTable
          rows={rows}
          loading={loading}
          onCellChange={onCellChange}
          onToggleActive={onToggleActive}
          onRemoveNewRow={onRemoveNewRow}
        />

        <ClientListFooter
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