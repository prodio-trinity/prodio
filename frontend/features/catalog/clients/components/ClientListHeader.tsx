import { Plus, Save } from "lucide-react";
import styles from "./ClientList.module.css";

interface ClientListHeaderProps {
  totalElements: number;
  dirtyCount: number;
  onAddRow: () => void;
  onSave: () => void;
  saving: boolean;
}

export function ClientListHeader({ totalElements, dirtyCount, onAddRow, onSave, saving }: ClientListHeaderProps) {
  return (
    <div className={styles.listHead}>
      <div className={styles.listHeadLeft}>
        <span className={styles.countChip}>조회결과 총 {totalElements.toLocaleString()}건</span>
        {dirtyCount > 0 ? <span className={styles.dirtyChip}>● 저장 안 된 변경 {dirtyCount}건</span> : null}
      </div>
      <div className={styles.toolbarRight}>
        <button type="button" className={styles.btn} onClick={onAddRow}>
          <Plus size={14} /> 행추가
        </button>
        <button
          type="button"
          className={`${styles.btn} ${styles.btnPrimary}`}
          disabled={dirtyCount === 0 || saving}
          onClick={onSave}
        >
          <Save size={14} /> {saving ? "저장 중..." : "저장"}
        </button>
      </div>
    </div>
  );
}