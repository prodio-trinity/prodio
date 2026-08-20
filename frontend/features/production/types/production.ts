export type ProductionStatus = "IN_PRODUCTION" | "IN_DELIVERY" | "COMPLETED";

export const PRODUCTION_STATUS_LABELS: Record<ProductionStatus, string> = {
  IN_PRODUCTION: "생산 중",
  IN_DELIVERY: "배송 중",
  COMPLETED: "완료",
};

export interface ProductionRecord {
  id: string;
  orderId: string;
  status: ProductionStatus;
  memo: string | null;
  phone: string | null;
  startedAt: string;
  shippedAt: string | null;
  completedAt: string | null;
}

export interface ProductionPage {
  records: ProductionRecord[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ProductionFilters {
  status?: ProductionStatus;
  page?: number;
  size?: number;
}

export interface ProductionActionResult {
  smsSent: boolean;
}
