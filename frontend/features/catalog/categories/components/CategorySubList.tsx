import type { CatalogSubCategory } from "@/features/catalog/products/utils/product";
import { AddSubCategoryRow } from "./AddSubCategoryRow";
import { SubCategoryRow } from "./SubCategoryRow";
import styles from "./CategoryTree.module.css";

interface CategorySubListProps {
  subs: CatalogSubCategory[];

  isAdding: boolean;
  addCode: string;
  onAddCodeChange: (value: string) => void;
  addName: string;
  onAddNameChange: (value: string) => void;
  addSaving: boolean;
  onCancelAdd: () => void;
  onSubmitAdd: () => void;

  editingId: number | null;
  editName: string;
  onEditNameChange: (value: string) => void;
  editSaving: boolean;
  onStartEdit: (sub: CatalogSubCategory) => void;
  onCancelEdit: () => void;
  onSubmitEdit: (sub: CatalogSubCategory) => void;

  onToggleActive: (sub: CatalogSubCategory, checked: boolean) => void;
}

export function CategorySubList({
  subs,
  isAdding,
  addCode,
  onAddCodeChange,
  addName,
  onAddNameChange,
  addSaving,
  onCancelAdd,
  onSubmitAdd,
  editingId,
  editName,
  onEditNameChange,
  editSaving,
  onStartEdit,
  onCancelEdit,
  onSubmitEdit,
  onToggleActive,
}: CategorySubListProps) {
  if (subs.length === 0 && !isAdding) {
    return (
      <div className={styles.subList}>
        <p className={styles.emptyMsg}>등록된 소분류가 없습니다.</p>
      </div>
    );
  }

  return (
    <div className={styles.subList}>
      {subs.map((sub) => (
        <SubCategoryRow
          key={sub.id}
          sub={sub}
          editing={editingId === sub.id}
          editName={editName}
          onEditNameChange={onEditNameChange}
          editSaving={editSaving}
          onStartEdit={() => onStartEdit(sub)}
          onCancelEdit={onCancelEdit}
          onSubmitEdit={() => onSubmitEdit(sub)}
          onToggleActive={(checked) => onToggleActive(sub, checked)}
        />
      ))}

      {isAdding ? (
        <AddSubCategoryRow
          code={addCode}
          onCodeChange={onAddCodeChange}
          name={addName}
          onNameChange={onAddNameChange}
          saving={addSaving}
          onCancel={onCancelAdd}
          onSubmit={onSubmitAdd}
        />
      ) : null}
    </div>
  );
}
