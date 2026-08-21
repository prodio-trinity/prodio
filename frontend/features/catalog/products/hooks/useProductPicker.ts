import { useEffect, useRef, useState } from "react";
import { productSearchService } from "../services/productSearchService";
import {
  addOrIncrementItem,
  type CatalogProductSearchItem,
  type CatalogSubCategory,
  type CatalogTopCategory,
  type SelectedProduct,
} from "../utils/product";

const DEBOUNCE_MS = 300;

export function useProductPicker(items: SelectedProduct[], onItemsChange: (items: SelectedProduct[]) => void) {
  // 카테고리 상태
  const [topCategories, setTopCategories] = useState<CatalogTopCategory[]>([]);
  const [subCategories, setSubCategories] = useState<CatalogSubCategory[]>([]);
  // 현재 선택한 카테고리
  const [selectedTop, setSelectedTop] = useState("");
  const [selectedSubId, setSelectedSubId] = useState("");

  // 검색 상태
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<CatalogProductSearchItem[]>([]);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [searching, setSearching] = useState(false);

  const debounceTimer = useRef<number | undefined>(undefined);

  useEffect(() => {
    void productSearchService.getCategories(true).then((result) => {
      // 카테고리 가져오기
      setTopCategories(result.topCategories);
      setSubCategories(result.subCategories);
    });
  }, []);

  // 화면에 보여줄 소분류 결정
  const visibleSubCategories = subCategories.filter(
    (sub) => sub.active &&
        (selectedTop === "" || sub.topCategory === selectedTop),
  );

  function runSearch(nextKeyword: string, categoryId: string) {
    window.clearTimeout(debounceTimer.current);
    if (!nextKeyword.trim()) {
      setResults([]);
      setDropdownOpen(false);
      return;
    }
    // 검색 시작
    setSearching(true);
    productSearchService
      .search({ keyword: nextKeyword, categoryId: categoryId ? Number(categoryId) : undefined })
      .then((products) => {
        setResults(products);
        setDropdownOpen(true);
      })
      .finally(() => setSearching(false));
  }

  function changeKeyword(value: string) {
    // 입력한 검색어 상태 저장
    setKeyword(value);
    window.clearTimeout(debounceTimer.current);
    if (!value.trim()) {
      setResults([]);
      setDropdownOpen(false);
      return;
    }
    debounceTimer.current = window.setTimeout(() => runSearch(value, selectedSubId), DEBOUNCE_MS);
  }

  function changeTopCategory(code: string) {
    setSelectedTop(code);
    setSelectedSubId(""); // 소분류 초기화
    runSearch(keyword, "");
  }

  function changeSubCategory(id: string) {
    setSelectedSubId(id);
    runSearch(keyword, id);
  }

  function submitSearch() {
    runSearch(keyword, selectedSubId);
  }

  function selectProduct(product: CatalogProductSearchItem) {
    onItemsChange(addOrIncrementItem(items, product));
    setKeyword("");
    setResults([]);
    setDropdownOpen(false);
  }

  function openDropdownIfResults() {
    if (results.length > 0) setDropdownOpen(true);
  }

  function closeDropdown() {
    setDropdownOpen(false);
  }

  return {
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
  };
}