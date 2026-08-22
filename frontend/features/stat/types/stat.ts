export type OrderViewStatus =
  | "PENDING"
  | "IN_PRODUCTION"
  | "IN_DELIVERY"
  | "COMPLETED"
  | "CANCELLED";

export const ORDER_VIEW_STATUS_LABELS: Record<OrderViewStatus, string> = {
  PENDING: "대기",
  IN_PRODUCTION: "생산중",
  IN_DELIVERY: "배송중",
  COMPLETED: "완료",
  CANCELLED: "취소",
};

export type SourceType =
  | "ORDER_NOTE"
  | "CLIENT_MEMO"
  | "PRODUCTION_MEMO"
  | "ALL";

export const SOURCE_TYPE_LABELS: Record<SourceType, string> = {
  ORDER_NOTE: "주문 노트",
  CLIENT_MEMO: "고객 메모",
  PRODUCTION_MEMO: "생산 메모",
  ALL: "전체",
};

export interface StatFilters {
  from?: string;
  to?: string;
  status?: OrderViewStatus;
}

export interface DashboardSummary {
  pendingCount: number;
  inProductionCount: number;
  inDeliveryCount: number;
  completedCount: number;
  cancelledCount: number;
  totalCount: number;
  completedQuantity: number;
}

export interface ProductDistribution {
  productId: string;
  productName: string;
  orderCount: number;
  totalQuantity: number;
}

export interface AiSummaryLog {
  id: string;
  question: string;
  response: string;
  requestedAt: string;
}

export interface AiSummaryLogPage {
  logs: AiSummaryLog[];
  page: number;
  size: number;
  totalElements: number;
}

export interface RagQaLog {
  id: string;
  sourceType: SourceType | null;
  question: string;
  response: string;
  requestedAt: string;
}

export interface RagQaLogPage {
  logs: RagQaLog[];
  page: number;
  size: number;
  totalElements: number;
}
