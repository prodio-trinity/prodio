import { useCallback, useEffect, useRef, useState } from "react";
import { catalogClientAdminService } from "../services/catalogClientAdminService";
import type { ClientFilters, EditableClientRow } from "../types/client";
import {
  emptyEditableRow,
  initialFilters,
  mergeFailedDrafts,
  toBulkUpsertRequest,
  toEditableRow,
  type EditableTextField,
} from "../utils/clientRow";

export function useClientList() {
  // 조회에 사용되는 조건
  const [filters, setFilters] = useState<ClientFilters>(initialFilters);
  // 검색창에 입력 중인 값
  const [keywordInput, setKeywordInput] = useState("");

  // 거래처 목록 기능
  const [rows, setRows] = useState<EditableClientRow[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  // 저장 기능
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [saveMessage, setSaveMessage] = useState("");
  const [toastId, setToastId] = useState(0);

  const nextTempId = useRef(-1);

  const load = useCallback(
    () =>
      catalogClientAdminService
        .list(filters)
        .then((result) => {
          setRows(result.clients.map(toEditableRow));
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

  const patchRow = (id: number, patch: Partial<EditableClientRow>) =>
    setRows((current) =>
      current.map((row) => (row.id !== id ? row : { ...row, ...patch, dirty: row.isNew ? row.dirty : true, error: null })),
    );

  const addRow = () => setRows((current) => [...current, emptyEditableRow(nextTempId.current--)]);
  const removeNewRow = (id: number) => setRows((current) => current.filter((row) => row.id !== id));
  const updateCell = (id: number, field: EditableTextField, value: string) => patchRow(id, { [field]: value });
  const toggleActive = (id: number, checked: boolean) => patchRow(id, { isActive: checked });

  const dirtyCount = rows.filter((row) => row.dirty || row.isNew).length;

  async function save() {
    // 저장할 행만 선택
    const targets = rows.filter((row) => row.dirty || row.isNew);
    if (targets.length === 0) return;
    setSaving(true);
    setSaveError("");
    setSaveMessage("");
    try {
      const results = await catalogClientAdminService.upsertBulk(targets.map(toBulkUpsertRequest));
      // 실패한 행만 따로 선택 (입력했던 내용 잃어버리지 않기 위함)
      const failedDrafts = targets
        .map((row, index) => ({ row, result: results[index] }))
        .filter(({ result }) => !result.success)
        .map(({ row, result }) => ({ ...row, dirty: true, error: result.reason ?? "저장 실패" }));

      // 실패한 건 표시
      if (failedDrafts.length > 0) {
        setSaveError(failedDrafts.map((row) => `${row.companyName || "(회사명 없음)"}: ${row.error}`).join(" / "));
      } else {
        setSaveMessage(`${targets.length}건 저장했습니다.`);
        setToastId((id) => id + 1);
      }

      // 성공한 행은 서버가 매긴 id/거래처코드를 확인해야 하니 항상 재조회.
      // 실패한 행은 재조회로 사라지니(신규행) 혹은 옛 값으로 덮이니(기존행) 로컬 draft를 다시 얹음
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

  // 조회 조건 변경되는 세 곳에서만 loading을 true로 되돌림
  const setIsActiveFilter = (value: boolean | null) => {
    setLoading(true);
    setFilters((current) => ({ ...current, isActive: value, page: 0 }));
  };
  const submitSearch = () => {
    setLoading(true);
    setFilters((current) => ({ ...current, keyword: keywordInput.trim(), page: 0 }));
  };
  const goToPage = (page: number) => {
    setLoading(true);
    setFilters((current) => ({ ...current, page }));
  };

  return {
    filters,
    keywordInput,
    setKeywordInput,
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
    updateCell,
    toggleActive,
    save,
    load,
    setIsActiveFilter,
    submitSearch,
    goToPage,
  };
}