import { Plus } from "lucide-react";
import gridStyles from "@/features/catalog/shared/components/AdminGrid.module.css";
import type { CatalogSubCategory, CatalogTopCategory } from "@/features/catalog/products/utils/product";
import { CategorySubList } from "./CategorySubList";
import styles from "./CategoryTree.module.css";

interface CategoryTreeProps {
  topCategories: CatalogTopCategory[];
  subCategories: CatalogSubCategory[];
  expanded: Set<string>;
  onToggleExpanded: (code: string) => void;

  addingUnder: string | null;
  addCode: string;
  onAddCodeChange: (value: string) => void;
  addName: string;
  onAddNameChange: (value: string) => void;
  addSaving: boolean;
  onStartAdd: (topCode: string) => void;
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

export function CategoryTree({
  topCategories,
  subCategories,
  expanded,
  onToggleExpanded,
  addingUnder,
  addCode,
  onAddCodeChange,
  addName,
  onAddNameChange,
  addSaving,
  onStartAdd,
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
}: CategoryTreeProps) {
  return (
    <div className={styles.tree}>
      {topCategories.map((top) => {
        const subs = subCategories.filter((sub) => sub.topCategory === top.code);
        const isOpen = expanded.has(top.code);

        return (
          <div key={top.code}>
            <div className={styles.topRow} onClick={() => onToggleExpanded(top.code)}>
              <span className={`${styles.caret} ${isOpen ? styles.caretOpen : ""}`}>▶</span>
              <span className={styles.topName}>{top.displayName}</span>
              <span className={styles.topCode}>{top.code}</span>
              <span className={styles.subCount}>{subs.length}개</span>
              <button
                type="button"
                className={gridStyles.btn}
                onClick={(event) => {
                  event.stopPropagation();
                  onStartAdd(top.code);
                }}
              >
                <Plus size={14} /> 소분류 추가
              </button>
            </div>

            {isOpen ? (
              <CategorySubList
                subs={subs}
                isAdding={addingUnder === top.code}
                addCode={addCode}
                onAddCodeChange={onAddCodeChange}
                addName={addName}
                onAddNameChange={onAddNameChange}
                addSaving={addSaving}
                onCancelAdd={onCancelAdd}
                onSubmitAdd={onSubmitAdd}
                editingId={editingId}
                editName={editName}
                onEditNameChange={onEditNameChange}
                editSaving={editSaving}
                onStartEdit={onStartEdit}
                onCancelEdit={onCancelEdit}
                onSubmitEdit={onSubmitEdit}
                onToggleActive={onToggleActive}
              />
            ) : null}
          </div>
        );
      })}
    </div>
  );
}
