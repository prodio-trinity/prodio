"use client";

import { useClientExcel } from "../hooks/useClientExcel";
import { useClientList } from "../hooks/useClientList";
import { ClientListView } from "./ClientListView";
import { ExcelUploadModal } from "./ExcelUploadModal";

export function ClientListPage() {
  const list = useClientList();
  const excel = useClientExcel(list.filters);

  return (
    <>
      <ClientListView
        keyword={list.keywordInput}
        onKeywordChange={list.setKeywordInput}
        isActive={list.filters.isActive}
        onIsActiveChange={list.setIsActiveFilter}
        onSearchSubmit={list.submitSearch}
        rows={list.rows}
        loading={list.loading}
        loadError={list.loadError}
        onCellChange={list.updateCell}
        onToggleActive={list.toggleActive}
        onRemoveNewRow={list.removeNewRow}
        totalElements={list.totalElements}
        dirtyCount={list.dirtyCount}
        page={list.filters.page}
        totalPages={list.totalPages}
        onPageChange={list.goToPage}
        onAddRow={list.addRow}
        onSave={() => void list.save()}
        saving={list.saving}
        saveError={list.saveError}
        saveMessage={list.saveMessage}
        toastId={list.toastId}
        exportError={excel.exportError}
        onOpenUpload={excel.openUpload}
        onExport={() => void excel.exportExcel()}
        exporting={excel.exporting}
      />

      {excel.uploadModalOpen ? (
        <ExcelUploadModal onClose={excel.closeUpload} onUploaded={() => void list.load()} />
      ) : null}
    </>
  );
}