"use client";

import { useState } from "react";
import type { OrderClientContext, OrderProductContext } from "../types/order";
import styles from "./orders.module.css";

export type OrderItemSelection = { productId: number; quantity: number };

export function OrderCatalogSection({ client, products, items, vatIncluded,
  onItemsChangeAction }: {
  client: OrderClientContext;
  products: OrderProductContext[];
  items: OrderItemSelection[];
  vatIncluded: boolean;
  onItemsChangeAction: (items: OrderItemSelection[]) => void;
}) {
  const [selectedProductId, setSelectedProductId] = useState("");
  const subtotal = items.reduce((sum, item) => {
    const product = products.find((value) => Number(value.productId) === item.productId);
    return sum + (product?.unitPrice ?? 0) * item.quantity;
  }, 0);
  const total = Math.round(subtotal * (vatIncluded ? 1.1 : 1));

  function addProduct() {
    const productId = Number(selectedProductId);
    if (!productId || items.some((item) => item.productId === productId)) return;
    onItemsChangeAction([...items, { productId, quantity: 1 }]);
    setSelectedProductId("");
  }

  function changeQuantity(productId: number, quantity: number) {
    onItemsChangeAction(items.map((item) => item.productId === productId
      ? { ...item, quantity: Math.max(1, quantity || 1) } : item));
  }

  return <section className={styles.card}>
    <h2>1. 수주 정보</h2>
    <div className={styles.orderClientInfo}>
      <div><span>수주처</span><strong>{client.companyName}</strong><small>{client.clientCode}</small></div>
      <div><span>대표자</span><strong>{client.representative || "-"}</strong><small>{client.businessRegistrationNumber || "사업자번호 미등록"}</small></div>
      <div><span>담당자</span><strong>{client.managerName || "-"}</strong><small>{client.phone || "연락처 미등록"}</small></div>
      <p>{client.defaultAddress || "등록된 본사 주소가 없습니다."}</p>
    </div>
    <div className={styles.productPicker}>
      <label>물품 선택<select value={selectedProductId} onChange={(event) => setSelectedProductId(event.target.value)}>
        <option value="">주문할 물품을 선택하세요</option>
        {products.filter((product) => !items.some((item) => item.productId === Number(product.productId)))
          .map((product) => <option key={product.productId} value={product.productId}>
            {product.name} · {product.unitPrice.toLocaleString("ko-KR")}원/{product.unit}
          </option>)}
      </select></label>
      <button type="button" onClick={addProduct} disabled={!selectedProductId}>품목 추가</button>
    </div>
    {products.length === 0 && <p className={styles.emptyInline}>현재 주문 가능한 물품이 없습니다.</p>}
    {items.length > 0 && <div className={styles.orderItems}>
      {items.map((item) => {
        const product = products.find((value) => Number(value.productId) === item.productId);
        return <article key={item.productId}>
          <div><strong>{product?.name ?? `물품 #${item.productId}`}</strong><span>{product?.productCode} · {product?.description || "설명 없음"}</span></div>
          <label>수량<input type="number" min="1" value={item.quantity} onChange={(event) => changeQuantity(item.productId, Number(event.target.value))} /></label>
          <strong>{product ? (product.unitPrice * item.quantity).toLocaleString("ko-KR") : "-"}원</strong>
          <button type="button" onClick={() => onItemsChangeAction(items.filter((value) => value.productId !== item.productId))}>삭제</button>
        </article>;
      })}
      <div className={styles.orderTotal}><span>예상 주문 금액{vatIncluded ? " (부가세 포함)" : ""}</span><strong>{total.toLocaleString("ko-KR")}원</strong></div>
    </div>}
  </section>;
}
