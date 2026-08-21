import { useCallback, useEffect, useRef, useState } from "react";
import { catalogProductAdminService } from "../services/catalogProductAdminService";
import { productSearchService } from "../services/productSearchService";
import type { CatalogSubCategory, CatalogTopCategory } from "../utils/product";
import {
  emptyEditableRow,
  initialFilters,
  mergeFailedDrafts,
  toBulkUpsertRequest,
  toEditableRow,
  type EditableProductRow,
  type ProductFilters,
} from "../utils/productRow";

export function useProductList() {
  const [filters, setFilters] = useState<ProductFilters>(initialFilters);
  const [keywordInput, setKeywordInput] = useState("");

  // 대분류는 검색 파라미터가 아니라 소분류 select 옵션을 좁히는 UI 보조 역할
  const [topCategories, setTopCategories] = useState<CatalogTopCategory[]>([]);
  const [subCategories, setSubCategories] = useState<CatalogSubCategory[]>([]);
  const [selectedTop, setSelectedTop] = useState("");

  const [rows, setRows] = useState<EditableProductRow[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [saveMessage, setSaveMessage] = useState("");
  const [toastId, setToastId] = useState(0);

  const nextTempId = useRef(-1);

  useEffect(() => {
    void productSearchService.getCategories(true).then((result) => {
      setTopCategories(result.topCategories);
      setSubCategories(result.subCategories);
    });
  }, []);

  const visibleSubCategories = subCategories.filter((sub) => selectedTop === "" || sub.topCategory === selectedTop);

  const load = useCallback(
    () =>
      catalogProductAdminService
        .list(filters)
        .then((result) => {
          setRows(result.products.map(toEditableRow));
          setTotalElements(result.totalElements);
          setTotalPages(result.totalPages);
          setLoadError("");
        })
        .catch((cause: unknown) => setLoadError(cause instanceof Error ? cause.message : "목록을 불러오지 못했습니다."))
        .finally(() => setLoading(false)),
    [filters],
  );

  useEffect(() => {
    void load();
  }, [load]);

  const patchRow = (id: number, patch: Partial<EditableProductRow>) =>
    setRows((current) =>
      current.map((row) => (row.id !== id ? row : { ...row, ...patch, dirty: row.isNew ? row.dirty : true, error: null })),
    );

  const addRow = () => setRows((current) => [...current, emptyEditableRow(nextTempId.current--)]);
  const removeNewRow = (id: number) => setRows((current) => current.filter((row) => row.id !== id));
  const updateProductName = (id: number, value: string) => patchRow(id, { productName: value });
  const updateCategory = (id: number, subCategoryId: number | null) => patchRow(id, { subCategoryId });
  const updateUnit = (id: number, unit: string) => patchRow(id, { unit });
  const updateUnitPrice = (id: number, unitPrice: number) => patchRow(id, { unitPrice: Math.max(0, unitPrice || 0) });
  const toggleActive = (id: number, checked: boolean) => patchRow(id, { isActive: checked });

  const dirtyCount = rows.filter((row) => row.dirty || row.isNew).length;

  async function save() {
    const targets = rows.filter((row) => row.dirty || row.isNew);
    if (targets.length === 0) return;
    setSaving(true);
    setSaveError("");
    setSaveMessage("");
    try {
      const requests = targets.map((row) => toBulkUpsertRequest(row, subCategories));
      const results = await catalogProductAdminService.upsertBulk(requests);
      const failedDrafts = targets
        .map((row, index) => ({ row, result: results[index] }))
        .filter(({ result }) => !result.success)
        .map(({ row, result }) => ({ ...row, dirty: true, error: result.reason ?? "저장 실패" }));

      if (failedDrafts.length > 0) {
        setSaveError(failedDrafts.map((row) => `${row.productName || "(품목명 없음)"}: ${row.error}`).join(" / "));
      } else {
        setSaveMessage(`${targets.length}건 저장했습니다.`);
        setToastId((id) => id + 1);
      }

      await load();
      if (failedDrafts.length > 0) {
        setRows((current) => mergeFailedDrafts(current, failedDrafts));
      }
    } catch (cause) {
      setSaveError(cause instanceof Error ? cause.message : "저장에 실패했습니다.");
    } finally {
      setSaving(false);
    }
  }

  // 재조회를 유발하는 곳에서만 loading을 true로 되돌림
  const changeTopCategoryFilter = (code: string) => {
    setSelectedTop(code);
    setLoading(true);
    setFilters((current) => ({ ...current, categoryId: null, page: 0 }));
  };
  const changeSubCategoryFilter = (id: string) => {
    setLoading(true);
    setFilters((current) => ({ ...current, categoryId: id ? Number(id) : null, page: 0 }));
  };
  const setIsActiveFilter = (value: boolean | null) => {
    setLoading(true);
    setFilters((current) => ({ ...current, isActive: value, page: 0 }));
  };
  const submitSearch = () => {
    setLoading(true);
    setFilters((current) => ({ ...current, keyword: keywordInput.trim(), page: 0 }));
  };
  const resetFilters = () => {
    setKeywordInput("");
    setSelectedTop("");
    setLoading(true);
    setFilters(initialFilters());
  };
  const goToPage = (page: number) => {
    setLoading(true);
    setFilters((current) => ({ ...current, page }));
  };

  return {
    filters,
    keywordInput,
    setKeywordInput,
    topCategories,
    subCategories,
    visibleSubCategories,
    selectedTop,
    rows,
    totalElements,
    totalPages,
    loading,
    loadError,
    saving,
    saveError,
    saveMessage,
    toastId,
    dirtyCount,
    addRow,
    removeNewRow,
    updateProductName,
    updateCategory,
    updateUnit,
    updateUnitPrice,
    toggleActive,
    save,
    load,
    changeTopCategoryFilter,
    changeSubCategoryFilter,
    setIsActiveFilter,
    submitSearch,
    resetFilters,
    goToPage,
  };
}
