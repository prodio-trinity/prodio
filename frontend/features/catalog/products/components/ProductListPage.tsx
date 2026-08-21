"use client";

import { useProductList } from "../hooks/useProductList";
import { ProductListView } from "./ProductListView";

export function ProductListPage() {
  const list = useProductList();

  return (
    <ProductListView
      keyword={list.keywordInput}
      onKeywordChange={list.setKeywordInput}
      topCategories={list.topCategories}
      filterSubCategories={list.visibleSubCategories}
      allSubCategories={list.subCategories}
      selectedTop={list.selectedTop}
      onTopChange={list.changeTopCategoryFilter}
      selectedSubId={list.filters.categoryId === null ? "" : String(list.filters.categoryId)}
      onSubChange={list.changeSubCategoryFilter}
      isActive={list.filters.isActive}
      onIsActiveChange={list.setIsActiveFilter}
      onReset={list.resetFilters}
      onSearchSubmit={list.submitSearch}
      rows={list.rows}
      loading={list.loading}
      loadError={list.loadError}
      onProductNameChange={list.updateProductName}
      onCategoryChange={list.updateCategory}
      onUnitChange={list.updateUnit}
      onUnitPriceChange={list.updateUnitPrice}
      onToggleActive={list.toggleActive}
      onRemoveNewRow={list.removeNewRow}
      totalElements={list.totalElements}
      dirtyCount={list.dirtyCount}
      page={list.filters.page}
      totalPages={list.totalPages}
      onPageChange={list.goToPage}
      onAddRow={list.addRow}
      onSave={() => void list.save()}
      saving={list.saving}
      saveError={list.saveError}
      saveMessage={list.saveMessage}
      toastId={list.toastId}
    />
  );
}
