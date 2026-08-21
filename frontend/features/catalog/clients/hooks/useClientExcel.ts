import { useState } from "react";
import { catalogClientAdminService } from "../services/catalogClientAdminService";
import type { ClientFilters } from "../types/client";

export function useClientExcel(filters: ClientFilters) {
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [exportError, setExportError] = useState("");

  const openUpload = () => setUploadModalOpen(true);
  const closeUpload = () => setUploadModalOpen(false);

  async function exportExcel() {
    setExporting(true);
    setExportError("");
    try {
      await catalogClientAdminService.exportExcel(filters);
    } catch (cause) {
      setExportError(cause instanceof Error ? cause.message : "엑셀 다운로드에 실패했습니다.");
    } finally {
      setExporting(false);
    }
  }

  return { uploadModalOpen, exporting, exportError, openUpload, closeUpload, exportExcel };
}