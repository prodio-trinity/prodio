import { AdminGridFooter } from "@/features/catalog/shared/components/AdminGridFooter";
import { AdminGridHeader } from "@/features/catalog/shared/components/AdminGridHeader";
import gridStyles from "@/features/catalog/shared/components/AdminGrid.module.css";
import type { EditableClientRow } from "../types/client";
import type { EditableTextField } from "../utils/clientRow";
import { ClientGridTable } from "./ClientGridTable";
import { ClientSearch } from "./ClientSearch";
import { SaveToast } from "./SaveToast";

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
    <main className={gridStyles.shell}>
      <header className={gridStyles.header}>
        <span className={gridStyles.eyebrow}>CLIENTS</span>
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

      {loadError ? <p className={gridStyles.error}>{loadError}</p> : null}
      {exportError ? <p className={gridStyles.error}>{exportError}</p> : null}

      <section className={gridStyles.card}>
        <AdminGridHeader
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