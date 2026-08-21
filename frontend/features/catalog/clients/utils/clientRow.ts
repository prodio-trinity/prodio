import type { ClientBulkUpsertRequest, ClientFilters, ClientListItem, EditableClientRow } from "../types/client";

export type EditableTextField =
  | "companyName"
  | "ceoName"
  | "businessRegNo"
  | "phone"
  | "address"
  | "managerName"
  | "memo";

const PAGE_SIZE = 20;

export function initialFilters(): ClientFilters {
  return { keyword: "", isActive: null, page: 0, size: PAGE_SIZE };
}

export function toEditableRow(item: ClientListItem): EditableClientRow {
  return {
    id: item.id,
    clientCode: item.clientCode,
    companyName: item.companyName,
    ceoName: item.ceoName ?? "",
    businessRegNo: item.businessRegNo ?? "",
    phone: item.phone ?? "",
    address: item.address ?? "",
    managerName: item.managerName ?? "",
    memo: item.memo ?? "",
    isActive: item.isActive,
    linkedToAccount: item.linkedToAccount,
    createdAt: item.createdAt,
    dirty: false,
    isNew: false,
    error: null,
  };
}

export function emptyEditableRow(tempId: number): EditableClientRow {
  return {
    id: tempId,
    clientCode: "",
    companyName: "",
    ceoName: "",
    businessRegNo: "",
    phone: "",
    address: "",
    managerName: "",
    memo: "",
    isActive: true,
    linkedToAccount: false,
    createdAt: "",
    dirty: false,
    isNew: true,
    error: null,
  };
}

/**
 * 저장 후 재조회한 목록에 저장 실패한 행의 로컬 draft를 유지
 * 기존 행은 사용자가 수정한 값을 유지하고, 신규 행은 재조회 결과에 없으므로 다시 추가
 */
export function mergeFailedDrafts(
  rows: EditableClientRow[],
  failedDrafts: EditableClientRow[],
): EditableClientRow[] {
  const draftById = new Map(failedDrafts.map((row) => [row.id, row]));

  const merged = rows.map((row) => draftById.get(row.id) ?? row);

  const existingIds = new Set(rows.map((row) => row.id));

  const appended = failedDrafts.filter(
    (row) => row.isNew && !existingIds.has(row.id),
  );

  return [...merged, ...appended];
}

export function toBulkUpsertRequest(row: EditableClientRow): ClientBulkUpsertRequest {
  return {
    id: row.isNew ? null : row.id,
    companyName: row.companyName.trim(),
    ceoName: row.ceoName.trim() || null,
    businessRegNo: row.businessRegNo.trim() || null,
    phone: row.phone.trim() || null,
    address: row.address.trim() || null,
    managerName: row.managerName.trim() || null,
    memo: row.memo.trim() || null,
    isActive: row.isActive,
  };
}