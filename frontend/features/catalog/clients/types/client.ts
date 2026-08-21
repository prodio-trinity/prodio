export interface ClientListItem {
  id: number;
  clientCode: string;
  companyName: string;
  ceoName: string | null;
  businessRegNo: string | null;
  phone: string | null;
  address: string | null;
  managerName: string | null;
  memo: string | null;
  isActive: boolean;
  linkedToAccount: boolean;
  createdAt: string;
}

export interface ClientPage {
  clients: ClientListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** GET /api/admin/catalog/clients 쿼리파라미터 그대로. isActive는 null이면 전체 조회. */
export interface ClientFilters {
  keyword: string;
  isActive: boolean | null;
  page: number;
  size: number;
}

export interface ClientBulkUpsertRequest {
  id: number | null;
  companyName: string;
  ceoName: string | null;
  businessRegNo: string | null;
  phone: string | null;
  address: string | null;
  managerName: string | null;
  memo: string | null;
  isActive: boolean | null;
}

export interface ClientBulkUpsertResult {
  index: number;
  success: boolean;
  id: number | null;
  clientCode: string | null;
  reason: string | null;
}

export interface ExcelUploadResultRowError {
  row: number;
  reason: string;
}

export interface ExcelUploadResult {
  totalRows: number;
  successCount: number;
  failCount: number;
  errors: ExcelUploadResultRowError[];
}

/** 그리드 편집용 로컬 로우. 신규행은 id가 임시 음수값. */
export interface EditableClientRow {
  id: number;
  clientCode: string;
  companyName: string;
  ceoName: string;
  businessRegNo: string;
  phone: string;
  address: string;
  managerName: string;
  memo: string;
  isActive: boolean;
  linkedToAccount: boolean;
  createdAt: string;
  dirty: boolean;
  isNew: boolean;
  error: string | null;
}