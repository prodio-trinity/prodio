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
  clientContact: string | null;
  orderName: string | null;
  items: OrderItem[];
  vatIncluded: boolean;
  totalAmount: number;
  delivery: DeliveryAddress;
  note: string | null;
  status: OrderStatus;
  cancellationReason: string | null;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineAmount: number;
}

export interface DeliveryAddress {
  addressId: string | null;
  name: string;
  recipientName: string;
  recipientPhone: string;
  postalCode: string;
  addressLine1: string;
  addressLine2: string;
}

export interface DeliveryContext {
  clientId: string;
  clientName: string;
  headquarters: DeliveryAddress | null;
  recent: DeliveryAddress | null;
  savedAddresses: DeliveryAddress[];
}

export interface OrderClientContext {
  clientId: string;
  clientCode: string;
  companyName: string;
  representative: string;
  businessRegistrationNumber: string;
  defaultAddress: string;
  phone: string;
  managerName: string;
  memo: string;
}

export interface OrderProductContext {
  productId: string;
  productCode: string;
  name: string;
  subCategoryId: string;
  unit: string;
  description: string;
  memo: string;
  unitPrice: number;
}

export interface OrderFormContext {
  client: OrderClientContext;
  products: OrderProductContext[];
}

export interface OrderItemCommand {
  productId: string;
  quantity: number;
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
  orderName?: string;
  items: OrderItemCommand[];
  vatIncluded: boolean;
  delivery: DeliveryAddress;
  note?: string;
}

export interface UpdateOrderCommand {
  orderName?: string;
  items: OrderItemCommand[];
  vatIncluded: boolean;
  delivery: DeliveryAddress;
  note?: string;
}
