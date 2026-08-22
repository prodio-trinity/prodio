import { useState } from "react";
import { catalogProductAdminService } from "../services/catalogProductAdminService";
import type { ProductFilters } from "../utils/productRow";

export function useProductExcel(filters: ProductFilters) {
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [exportError, setExportError] = useState("");

  const openUpload = () => setUploadModalOpen(true);
  const closeUpload = () => setUploadModalOpen(false);

  async function exportExcel() {
    setExporting(true);
    setExportError("");
    try {
      await catalogProductAdminService.exportExcel(filters);
    } catch (cause) {
      setExportError(cause instanceof Error ? cause.message : "엑셀 다운로드에 실패했습니다.");
    } finally {
      setExporting(false);
    }
  }

  return { uploadModalOpen, exporting, exportError, openUpload, closeUpload, exportExcel };
}