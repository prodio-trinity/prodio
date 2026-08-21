import { Download, Upload } from "lucide-react";
import styles from "./ClientList.module.css";

interface ClientListFooterProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  onOpenUpload: () => void;
  onExport: () => void;
  exporting: boolean;
}

function pageNumbers(page: number, totalPages: number): number[] {
  const windowSize = 5;
  const start = Math.max(0, Math.min(page - Math.floor(windowSize / 2), totalPages - windowSize));
  const from = Math.max(0, start);
  const to = Math.min(totalPages, from + windowSize);
  return Array.from({ length: Math.max(0, to - from) }, (_, i) => from + i);
}

export function ClientListFooter({ page, totalPages, onPageChange, onOpenUpload, onExport, exporting }: ClientListFooterProps) {
  return (
    <div className={styles.footer}>
      <div className={styles.footerLeft}>
        <button type="button" className={styles.btn} onClick={onOpenUpload}>
          <Upload size={14} /> 엑셀 업로드
        </button>
        <button type="button" className={styles.btn} disabled={exporting} onClick={onExport}>
          <Download size={14} /> {exporting ? "다운로드 중..." : "엑셀 다운로드"}
        </button>
      </div>
      <div className={styles.pager}>
        <button type="button" onClick={() => onPageChange(page - 1)} disabled={page <= 0}>
          &lt;
        </button>
        {pageNumbers(page, Math.max(totalPages, 1)).map((p) => (
          <button
            key={p}
            type="button"
            className={p === page ? styles.pagerActive : ""}
            onClick={() => onPageChange(p)}
          >
            {p + 1}
          </button>
        ))}
        <button type="button" onClick={() => onPageChange(page + 1)} disabled={page + 1 >= Math.max(totalPages, 1)}>
          &gt;
        </button>
      </div>
      <span className={styles.footerHint}>셀을 클릭해서 수정한 뒤 [저장]을 눌러야 반영됩니다.</span>
    </div>
  );
}