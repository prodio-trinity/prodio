"use client";

import { FormEvent, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { orderService } from "../services/orderService";
import type { ClientOption, ProductOption } from "../types/order";
import { ClientSelector, ProductSelector } from "./catalog/DummyCatalogSelectors";
import styles from "./orders.module.css";

export function OrderForm() {
  const router = useRouter();
  const [client, setClient] = useState<ClientOption>();
  const [product, setProduct] = useState<ProductOption>();
  const [quantity, setQuantity] = useState(1);
  const [vatIncluded, setVatIncluded] = useState(true);
  const [dueDate, setDueDate] = useState("");
  const [deliveryAddress, setDeliveryAddress] = useState("");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const total = useMemo(() => product ? Math.round(product.unitPrice * quantity * (vatIncluded ? 1.1 : 1)) : 0, [product, quantity, vatIncluded]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!client || !product) return setError("수주처와 품목을 선택해 주세요.");
    setSaving(true); setError("");
    try {
      const order = await orderService.create({ clientId: String(client.id), productId: String(product.id), quantity, vatIncluded, dueDate, deliveryAddress, note });
      router.push(`/orders/${order.id}`);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "주문 등록에 실패했습니다."); }
    finally { setSaving(false); }
  }

  return (
    <main className={styles.shell}>
      <header className={styles.header}><div><span className={styles.eyebrow}>ORDER</span><h1>새 주문 등록</h1><p>수주 시점의 거래처·품목 정보를 주문에 스냅샷으로 보관합니다.</p></div></header>
      <form className={styles.form} onSubmit={submit}>
        <section className={styles.card}><h2>1. 수주 정보</h2><ClientSelector value={client?.id} onChange={setClient} /><ProductSelector value={product?.id} onChange={setProduct} /></section>
        <section className={styles.card}>
          <h2>2. 납품 및 금액</h2>
          <div className={styles.grid}>
            <label>수량<input type="number" min="1" value={quantity} onChange={(e) => setQuantity(Number(e.target.value))} required /></label>
            <label>납기일<input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} required /></label>
            <label className={styles.wide}>납품 주소<input value={deliveryAddress} onChange={(e) => setDeliveryAddress(e.target.value)} placeholder="선택 입력" /></label>
          </div>
          <label className={styles.check}><input type="checkbox" checked={vatIncluded} onChange={(e) => setVatIncluded(e.target.checked)} /> 부가세 10% 포함</label>
          <div className={styles.amount}><span>예상 주문 금액</span><strong>{total.toLocaleString("ko-KR")}원</strong></div>
        </section>
        <section className={styles.card}><h2>3. 메모</h2><textarea rows={4} value={note} onChange={(e) => setNote(e.target.value)} placeholder="생산 또는 납품 시 참고할 내용을 입력하세요." /></section>
        {error && <p className={styles.error}>{error}</p>}
        <div className={styles.actions}><button type="button" className={styles.secondary} onClick={() => router.back()}>취소</button><button disabled={saving}>{saving ? "등록 중..." : "주문 등록"}</button></div>
      </form>
    </main>
  );
}
