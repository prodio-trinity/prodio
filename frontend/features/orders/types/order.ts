export type OrderStatus = "PENDING_PAYMENT" | "CONFIRMED" | "CANCELLED";

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING_PAYMENT: "입금 대기",
  CONFIRMED: "주문 확정",
  CANCELLED: "주문 취소",
};

export interface Order {
  id: string;
  clientId: string;
  clientName: string;
  clientPhone: string | null;
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  vatIncluded: boolean;
  totalAmount: number;
  dueDate: string;
  deliveryAddress: string | null;
  note: string | null;
  status: OrderStatus;
  cancellationReason: string | null;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderPage {
  orders: Order[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface OrderFilters {
  status?: OrderStatus;
  q?: string;
  page?: number;
  size?: number;
}

export interface CreateOrderCommand {
  clientId: string;
  productId: string;
  quantity: number;
  vatIncluded: boolean;
  dueDate: string;
  deliveryAddress?: string;
  note?: string;
}

export interface UpdateOrderCommand {
  productId: string;
  quantity: number;
  vatIncluded: boolean;
  dueDate: string;
  deliveryAddress?: string;
  note?: string;
}
