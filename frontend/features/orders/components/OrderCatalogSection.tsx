"use client";

import { useState } from "react";
import type { OrderClientContext, OrderProductContext } from "../types/order";
import { OrderQuotationModal } from "./OrderQuotationModal";
import styles from "./orders.module.css";

export type OrderItemSelection = { productId: number; quantity: number };

export function OrderCatalogSection({ client, products, orderName, items, vatIncluded,
  onItemsChangeAction, onOrderNameChangeAction, onVatIncludedChangeAction }: {
  client: OrderClientContext;
  products: OrderProductContext[];
  orderName: string;
  items: OrderItemSelection[];
  vatIncluded: boolean;
  onItemsChangeAction: (items: OrderItemSelection[]) => void;
  onOrderNameChangeAction: (orderName: string) => void;
  onVatIncludedChangeAction: (vatIncluded: boolean) => void;
}) {
  const [selectedProductId, setSelectedProductId] = useState("");
  const [quotationOpen, setQuotationOpen] = useState(false);
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
    <div className={styles.sectionTitle}><h2>1. 수주 정보</h2><button type="button" className={styles.quotationButton} disabled={items.length === 0} onClick={() => setQuotationOpen(true)}>견적서 출력</button></div>
    <div className={styles.orderClientInfo}>
      <div><span>수주처 · {client.clientCode}</span><strong>{client.companyName}</strong></div>
      <div><span>대표자</span><strong>{client.representative || "-"}</strong></div>
      <div><span>담당자</span><strong>{client.managerName || "-"}</strong></div>
      <dl className={styles.clientMetaRow}>
        <div><dt>사업자등록번호</dt><dd>{client.businessRegistrationNumber || "-"}</dd></div>
        <div><dt>거래처 연락처</dt><dd>{client.phone || "-"}</dd></div>
      </dl>
      <p><span>사업장 위치</span><strong>{client.defaultAddress || "등록된 사업장 위치가 없습니다."}</strong></p>
    </div>
    <label className={styles.orderNameField}>주문서명 <span>선택 입력 · 미입력 시 일부 품목명으로 표시</span><input maxLength={200} value={orderName} onChange={(event) => onOrderNameChangeAction(event.target.value)} placeholder="예: 8월 본사 설비 교체 주문" /></label>
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
      <label className={styles.check}>
        <input type="checkbox" checked={vatIncluded} onChange={(event) => onVatIncludedChangeAction(event.target.checked)} /> 부가세 10% 포함
      </label>
      <div className={styles.orderTotal}><span>예상 주문 금액{vatIncluded ? " (부가세 포함)" : ""}</span><strong>{total.toLocaleString("ko-KR")}원</strong></div>
    </div>}
    {quotationOpen && <OrderQuotationModal client={client} products={products} items={items}
      vatIncluded={vatIncluded} onCloseAction={() => setQuotationOpen(false)} />}
  </section>;
}
