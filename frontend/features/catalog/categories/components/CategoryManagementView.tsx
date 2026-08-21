import Link from "next/link";
import gridStyles from "@/features/catalog/shared/components/AdminGrid.module.css";
import type { CatalogSubCategory, CatalogTopCategory } from "@/features/catalog/products/utils/product";
import { CategoryTree } from "./CategoryTree";

interface CategoryManagementViewProps {
  topCategories: CatalogTopCategory[];
  subCategories: CatalogSubCategory[];
  loading: boolean;
  loadError: string;
  actionError: string;

  includeInactive: boolean;
  onToggleIncludeInactive: (checked: boolean) => void;
  onExpandAll: () => void;
  onCollapseAll: () => void;

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

export function CategoryManagementView({
  topCategories,
  subCategories,
  loading,
  loadError,
  actionError,
  includeInactive,
  onToggleIncludeInactive,
  onExpandAll,
  onCollapseAll,
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
}: CategoryManagementViewProps) {
  return (
    <main className={gridStyles.shell}>
      <header className={gridStyles.header}>
        <Link className={gridStyles.btn} href="/catalog/products" style={{ width: "fit-content" }}>
          ← 뒤로가기
        </Link>
        <h1 style={{ marginTop: 8 }}>품목 카테고리</h1>
      </header>

      {loadError ? <p className={gridStyles.error}>{loadError}</p> : null}
      {actionError ? <p className={gridStyles.error}>{actionError}</p> : null}

      <section className={gridStyles.card}>
        <div className={gridStyles.listHead}>
          <div className={gridStyles.listHeadLeft}>
            <span className={gridStyles.countChip}>
              대분류 {topCategories.length} · 소분류 {subCategories.length}건
            </span>
          </div>
          <div className={gridStyles.toolbarRight}>
            <label className={`${gridStyles.activeBadge} ${gridStyles.activeBadgeOn}`}>
              <input
                type="checkbox"
                checked={includeInactive}
                onChange={(event) => onToggleIncludeInactive(event.target.checked)}
              />
              비활성 포함
            </label>
            <button type="button" className={gridStyles.btn} onClick={onExpandAll}>
              전체 펼치기
            </button>
            <button type="button" className={gridStyles.btn} onClick={onCollapseAll}>
              전체 접기
            </button>
          </div>
        </div>

        {loading ? (
          <p className={gridStyles.emptyRows}>불러오는 중...</p>
        ) : (
          <CategoryTree
            topCategories={topCategories}
            subCategories={subCategories}
            expanded={expanded}
            onToggleExpanded={onToggleExpanded}
            addingUnder={addingUnder}
            addCode={addCode}
            onAddCodeChange={onAddCodeChange}
            addName={addName}
            onAddNameChange={onAddNameChange}
            addSaving={addSaving}
            onStartAdd={onStartAdd}
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
        )}

        <div className={gridStyles.footer}>
          <span />
          <span />
          <span className={gridStyles.footerHint}>
            대분류는 고정값 · 소분류는 즉시 반영, 별도 저장 버튼 없음
          </span>
        </div>
      </section>
    </main>
  );
}
