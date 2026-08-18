export type OrderStatus = "PENDING" | "IN_PRODUCTION";

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
  paymentConfirmed: boolean;
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

export interface ClientOption {
  id: number;
  name: string;
  contact: string;
}

export interface ProductOption {
  id: number;
  name: string;
  unitPrice: number;
  specification: string;
}
