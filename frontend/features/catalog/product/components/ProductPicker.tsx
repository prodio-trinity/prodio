"use client";

import { useEffect, useRef } from "react";
import { useProductPicker } from "../hooks/useProductPicker";
import { removeItem, updateItemQuantity, type SelectedProduct } from "../utils/product";
import { ProductSearchPanel } from "./ProductSearchPanel";
import { SelectedProductTable } from "./SelectedProductTable";
import styles from "./ProductPicker.module.css";

interface ProductPickerProps {
  items: SelectedProduct[];
  onItemsChange: (items: SelectedProduct[]) => void;
}

export function ProductPicker({ items, onItemsChange }: ProductPickerProps) {
  const {
    topCategories,
    visibleSubCategories,
    selectedTop,
    selectedSubId,
    changeTopCategory,
    changeSubCategory,
    keyword,
    changeKeyword,
    submitSearch,
    results,
    searching,
    dropdownOpen,
    openDropdownIfResults,
    closeDropdown,
    selectProduct,
  } = useProductPicker(items, onItemsChange);

  const wrapRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    function closeOnOutsideClick(event: MouseEvent) {
      if (!wrapRef.current?.contains(event.target as Node)) {
        closeDropdown();
      }
    }
    document.addEventListener("mousedown", closeOnOutsideClick);
    return () => {
      document.removeEventListener("mousedown", closeOnOutsideClick);
    };
  }, [closeDropdown]);

  return (
    <div className={styles.wrap} ref={wrapRef}>
      <div className={styles.subheading}>품목 선택</div>
      <ProductSearchPanel
        topCategories={topCategories}
        subCategories={visibleSubCategories}
        selectedTop={selectedTop}
        selectedSubId={selectedSubId}
        onTopChange={changeTopCategory}
        onSubChange={changeSubCategory}
        keyword={keyword}
        onKeywordChange={changeKeyword}
        onSubmit={submitSearch}
        results={results}
        searching={searching}
        dropdownOpen={dropdownOpen}
        onFocus={openDropdownIfResults}
        onSelect={selectProduct}
      />
      <SelectedProductTable
        items={items}
        onQuantityChange={(productId, quantity) => onItemsChange(updateItemQuantity(items, productId, quantity))}
        onRemove={(productId) => onItemsChange(removeItem(items, productId))}
      />
    </div>
  );
}