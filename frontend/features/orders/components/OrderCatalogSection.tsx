"use client";

import { useState } from "react";
import { ProductPicker } from "@/features/catalog/product/components/ProductPicker";
import type { SelectedProduct } from "@/features/catalog/product/utils/product";
import type { OrderClientContext } from "../types/order";
import { OrderQuotationModal } from "./OrderQuotationModal";
import styles from "./orders.module.css";

export function OrderCatalogSection({ client, orderName, items, vatIncluded,
  onItemsChangeAction, onOrderNameChangeAction, onVatIncludedChangeAction }: {
  client: OrderClientContext;
  orderName: string;
  items: SelectedProduct[];
  vatIncluded: boolean;
  onItemsChangeAction: (items: SelectedProduct[]) => void;
  onOrderNameChangeAction: (orderName: string) => void;
  onVatIncludedChangeAction: (vatIncluded: boolean) => void;
}) {
  const [quotationOpen, setQuotationOpen] = useState(false);
  const subtotal = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
  const total = Math.round(subtotal * (vatIncluded ? 1.1 : 1));

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

    <ProductPicker items={items} onItemsChange={onItemsChangeAction} />

    {items.length > 0 && <>
      <label className={styles.check}>
        <input type="checkbox" checked={vatIncluded} onChange={(event) => onVatIncludedChangeAction(event.target.checked)} /> 부가세 10% 포함
      </label>
      <div className={styles.orderTotal}><span>예상 주문 금액{vatIncluded ? " (부가세 포함)" : ""}</span><strong>{total.toLocaleString("ko-KR")}원</strong></div>
    </>}
    {quotationOpen && <OrderQuotationModal client={client} items={items}
      vatIncluded={vatIncluded} onCloseAction={() => setQuotationOpen(false)} />}
  </section>;
}