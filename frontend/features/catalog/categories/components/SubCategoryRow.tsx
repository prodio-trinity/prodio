import { Check, Pencil, X } from "lucide-react";
import gridStyles from "@/features/catalog/shared/components/AdminGrid.module.css";
import type { CatalogSubCategory } from "@/features/catalog/products/utils/product";
import styles from "./CategoryTree.module.css";

interface SubCategoryRowProps {
  sub: CatalogSubCategory;
  editing: boolean;
  editName: string;
  onEditNameChange: (value: string) => void;
  editSaving: boolean;
  onStartEdit: () => void;
  onCancelEdit: () => void;
  onSubmitEdit: () => void;
  onToggleActive: (checked: boolean) => void;
}

export function SubCategoryRow({
  sub,
  editing,
  editName,
  onEditNameChange,
  editSaving,
  onStartEdit,
  onCancelEdit,
  onSubmitEdit,
  onToggleActive,
}: SubCategoryRowProps) {
  return (
    <div className={`${styles.subRow} ${sub.active ? "" : styles.subRowInactive}`}>
      <span className={styles.subCode}>{sub.subCategoryCode}</span>
      {editing ? (
        <input
          className={styles.inlineInput}
          value={editName}
          onChange={(event) => onEditNameChange(event.target.value)}
          placeholder="소분류명"
        />
      ) : (
        <span className={styles.subName}>{sub.name}</span>
      )}
      <span className={styles.subActions}>
        {editing ? (
          <>
            <button
              type="button"
              className={`${styles.iconBtn} ${styles.iconBtnConfirm}`}
              title="저장"
              disabled={editSaving}
              onClick={onSubmitEdit}
            >
              <Check size={13} />
            </button>
            <button type="button" className={styles.iconBtn} title="취소" onClick={onCancelEdit}>
              <X size={13} />
            </button>
          </>
        ) : (
          <button type="button" className={styles.iconBtn} title="이름 수정" onClick={onStartEdit}>
            <Pencil size={13} />
          </button>
        )}
        <label
          className={`${gridStyles.activeBadge} ${sub.active ? gridStyles.activeBadgeOn : gridStyles.activeBadgeOff}`}
        >
          <input type="checkbox" checked={sub.active} onChange={(event) => onToggleActive(event.target.checked)} />
          {sub.active ? "사용" : "미사용"}
        </label>
      </span>
    </div>
  );
}
