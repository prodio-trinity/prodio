"use client";

import { useState } from "react";
import { X } from "lucide-react";
import gridStyles from "@/features/catalog/shared/components/AdminGrid.module.css";
import { catalogClientAdminService } from "../services/catalogClientAdminService";
import type { ExcelUploadResult } from "../types/client";
import styles from "./ClientList.module.css";

interface ExcelUploadModalProps {
  onClose: () => void;
  onUploaded: () => void;
}

export function ExcelUploadModal({ onClose, onUploaded }: ExcelUploadModalProps) {
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState<ExcelUploadResult | null>(null);

  async function handleUpload() {
    if (!file) return;
    setUploading(true);
    setError("");
    try {
      const uploadResult = await catalogClientAdminService.uploadExcel(file);
      setResult(uploadResult);
      if (uploadResult.successCount > 0) onUploaded();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "엑셀 업로드에 실패했습니다.");
    } finally {
      setUploading(false);
    }
  }

  return (
    <div className={styles.modalBackdrop} onClick={onClose}>
      <div className={styles.modal} onClick={(event) => event.stopPropagation()}>
        <div className={styles.modalHead}>
          <h2>엑셀 업로드</h2>
          <button type="button" className={styles.modalClose} onClick={onClose}>
            <X size={16} />
          </button>
        </div>
        <div className={styles.modalBody}>
          <p style={{ fontSize: 12, color: "var(--color-text-secondary)" }}>
            컬럼 순서: 회사명(필수), 대표자, 사업자등록번호(필수), 연락처, 주소, 담당자, 비고. 사업자등록번호가 기존
            거래처와 일치하면 해당 거래처 정보가 갱신됩니다.
          </p>
          <input
            className={styles.fileInput}
            type="file"
            accept=".xlsx,.xls"
            onChange={(event) => {
              setFile(event.target.files?.[0] ?? null);
              setResult(null);
              setError("");
            }}
          />

          {error ? <p className={gridStyles.error}>{error}</p> : null}

          {result ? (
            <>
              <div className={styles.uploadSummary}>
                <span>
                  전체 <strong>{result.totalRows}</strong>건
                </span>
                <span style={{ color: "var(--color-success)" }}>
                  성공 <strong>{result.successCount}</strong>건
                </span>
                <span style={{ color: "var(--color-danger)" }}>
                  실패 <strong>{result.failCount}</strong>건
                </span>
              </div>
              {result.errors.length > 0 ? (
                <div className={styles.uploadErrorList}>
                  <table>
                    <thead>
                      <tr>
                        <th style={{ width: 70 }}>행</th>
                        <th>사유</th>
                      </tr>
                    </thead>
                    <tbody>
                      {result.errors.map((rowError) => (
                        <tr key={rowError.row}>
                          <td>{rowError.row}</td>
                          <td>{rowError.reason}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : null}
            </>
          ) : null}
        </div>
        <div className={styles.modalFoot}>
          <button type="button" className={gridStyles.btn} onClick={onClose}>
            닫기
          </button>
          <button
            type="button"
            className={`${gridStyles.btn} ${gridStyles.btnPrimary}`}
            disabled={!file || uploading || result !== null}
            onClick={() => void handleUpload()}
          >
            {uploading ? "업로드 중..." : "업로드"}
          </button>
        </div>
      </div>
    </div>
  );
}