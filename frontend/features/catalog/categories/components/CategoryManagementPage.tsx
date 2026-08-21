"use client";

import { useCategoryManagement } from "../hooks/useCategoryManagement";
import { CategoryManagementView } from "./CategoryManagementView";

export function CategoryManagementPage() {
  const category = useCategoryManagement();

  return (
    <CategoryManagementView
      topCategories={category.topCategories}
      subCategories={category.subCategories}
      loading={category.loading}
      loadError={category.loadError}
      actionError={category.actionError}
      includeInactive={category.includeInactive}
      onToggleIncludeInactive={category.toggleIncludeInactive}
      onExpandAll={category.expandAll}
      onCollapseAll={category.collapseAll}
      expanded={category.expanded}
      onToggleExpanded={category.toggleExpanded}
      addingUnder={category.addingUnder}
      addCode={category.addCode}
      onAddCodeChange={category.setAddCode}
      addName={category.addName}
      onAddNameChange={category.setAddName}
      addSaving={category.addSaving}
      onStartAdd={category.startAdd}
      onCancelAdd={category.cancelAdd}
      onSubmitAdd={() => void category.submitAdd()}
      editingId={category.editingId}
      editName={category.editName}
      onEditNameChange={category.setEditName}
      editSaving={category.editSaving}
      onStartEdit={category.startEdit}
      onCancelEdit={category.cancelEdit}
      onSubmitEdit={(sub) => void category.submitEdit(sub)}
      onToggleActive={(sub, checked) => void category.toggleActive(sub, checked)}
    />
  );
}
