import { useCallback, useEffect, useState } from "react";
import { productSearchService } from "@/features/catalog/products/services/productSearchService";
import type { CatalogSubCategory, CatalogTopCategory } from "@/features/catalog/products/utils/product";
import { categoryAdminService } from "../services/categoryAdminService";

export function useCategoryManagement() {
  const [topCategories, setTopCategories] = useState<CatalogTopCategory[]>([]);
  const [subCategories, setSubCategories] = useState<CatalogSubCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [includeInactive, setIncludeInactive] = useState(true);

  const [expanded, setExpanded] = useState<Set<string>>(new Set());

  const [addingUnder, setAddingUnder] = useState<string | null>(null);
  const [addCode, setAddCode] = useState("");
  const [addName, setAddName] = useState("");
  const [addSaving, setAddSaving] = useState(false);

  const [editingId, setEditingId] = useState<number | null>(null);
  const [editName, setEditName] = useState("");
  const [editSaving, setEditSaving] = useState(false);

  const [actionError, setActionError] = useState("");

  const load = useCallback(
    () =>
      productSearchService
        .getCategories(includeInactive ? undefined : true)
        .then((result) => {
          setTopCategories(result.topCategories);
          setSubCategories(result.subCategories);
          setLoadError("");
        })
        .catch((cause: unknown) => setLoadError(cause instanceof Error ? cause.message : "카테고리를 불러오지 못했습니다."))
        .finally(() => setLoading(false)),
    [includeInactive],
  );

  useEffect(() => {
    void load();
  }, [load]);

  function toggleIncludeInactive(checked: boolean) {
    setLoading(true);
    setIncludeInactive(checked);
  }

  function toggleExpanded(code: string) {
    setExpanded((current) => {
      const next = new Set(current);
      if (next.has(code)) next.delete(code);
      else next.add(code);
      return next;
    });
  }

  function expandAll() {
    setExpanded(new Set(topCategories.map((top) => top.code)));
  }

  function collapseAll() {
    setExpanded(new Set());
  }

  function startAdd(topCode: string) {
    setExpanded((current) => new Set(current).add(topCode));
    setAddingUnder(topCode);
    setAddCode("");
    setAddName("");
    setActionError("");
  }

  function cancelAdd() {
    setAddingUnder(null);
  }

  async function submitAdd() {
    if (!addingUnder) return;
    const code = addCode.trim().toUpperCase();
    const name = addName.trim();
    if (!code || !name) {
      setActionError("소분류 코드와 이름을 모두 입력해 주세요.");
      return;
    }
    setAddSaving(true);
    setActionError("");
    try {
      await categoryAdminService.create(addingUnder, code, name);
      setAddingUnder(null);
      await load();
    } catch (cause) {
      setActionError(cause instanceof Error ? cause.message : "소분류 등록에 실패했습니다.");
    } finally {
      setAddSaving(false);
    }
  }

  function startEdit(sub: CatalogSubCategory) {
    setEditingId(sub.id);
    setEditName(sub.name);
    setActionError("");
  }

  function cancelEdit() {
    setEditingId(null);
  }

  async function submitEdit(sub: CatalogSubCategory) {
    const name = editName.trim();
    if (!name) {
      setActionError("소분류 이름을 입력해 주세요.");
      return;
    }
    setEditSaving(true);
    setActionError("");
    try {
      await categoryAdminService.update(sub.id, name, sub.active);
      setEditingId(null);
      await load();
    } catch (cause) {
      setActionError(cause instanceof Error ? cause.message : "소분류 수정에 실패했습니다.");
    } finally {
      setEditSaving(false);
    }
  }

  async function toggleActive(sub: CatalogSubCategory, checked: boolean) {
    if (!checked && !window.confirm(`'${sub.name}' 소분류를 비활성화할까요?`)) {
      return;
    }
    setActionError("");
    try {
      await categoryAdminService.update(sub.id, sub.name, checked);
      await load();
    } catch (cause) {
      setActionError(cause instanceof Error ? cause.message : "사용여부 변경에 실패했습니다.");
    }
  }

  return {
    topCategories,
    subCategories,
    loading,
    loadError,
    includeInactive,
    toggleIncludeInactive,
    expanded,
    toggleExpanded,
    expandAll,
    collapseAll,
    addingUnder,
    addCode,
    setAddCode,
    addName,
    setAddName,
    addSaving,
    startAdd,
    cancelAdd,
    submitAdd,
    editingId,
    editName,
    setEditName,
    editSaving,
    startEdit,
    cancelEdit,
    submitEdit,
    toggleActive,
    actionError,
  };
}
