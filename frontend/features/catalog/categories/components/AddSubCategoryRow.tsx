import { Check, X } from "lucide-react";
import styles from "./CategoryTree.module.css";

interface AddSubCategoryRowProps {
  code: string;
  onCodeChange: (value: string) => void;
  name: string;
  onNameChange: (value: string) => void;
  saving: boolean;
  onCancel: () => void;
  onSubmit: () => void;
}

export function AddSubCategoryRow({
  code,
  onCodeChange,
  name,
  onNameChange,
  saving,
  onCancel,
  onSubmit,
}: AddSubCategoryRowProps) {
  return (
    <div className={`${styles.subRow} ${styles.newRow}`}>
      <input
        className={`${styles.inlineInput} ${styles.inlineCode}`}
        value={code}
        onChange={(event) => onCodeChange(event.target.value)}
        placeholder="코드 (예: MOUSE)"
      />
      <input
        className={styles.inlineInput}
        value={name}
        onChange={(event) => onNameChange(event.target.value)}
        placeholder="소분류명"
      />
      <span className={styles.subActions}>
        <button
          type="button"
          className={`${styles.iconBtn} ${styles.iconBtnConfirm}`}
          title="저장"
          disabled={saving}
          onClick={onSubmit}
        >
          <Check size={13} />
        </button>
        <button type="button" className={styles.iconBtn} title="취소" onClick={onCancel}>
          <X size={13} />
        </button>
      </span>
    </div>
  );
}
