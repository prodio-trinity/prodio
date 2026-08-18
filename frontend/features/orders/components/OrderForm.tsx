"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { CatalogOrderSection } from "@/features/catalog/components/order-selection/CatalogOrderSection";
import { orderService } from "../services/orderService";
import { OrderDetailsSection } from "./OrderDetailsSection";
import styles from "./orders.module.css";

export function OrderForm() {
  const router = useRouter();
  const [clientId, setClientId] = useState<number>();
  const [productId, setProductId] = useState<number>();
  const [quantity, setQuantity] = useState(1);
  const [vatIncluded, setVatIncluded] = useState(true);
  const [dueDate, setDueDate] = useState("");
  const [deliveryAddress, setDeliveryAddress] = useState("");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!clientId || !productId) return setError("수주처와 품목을 선택해 주세요.");
    setSaving(true); setError("");
    try {
      const order = await orderService.create({ clientId: String(clientId), productId: String(productId), quantity, vatIncluded, dueDate, deliveryAddress, note });
      router.push(`/orders/${order.id}`);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "주문 등록에 실패했습니다."); }
    finally { setSaving(false); }
  }

  return (
    <main className={styles.shell}>
      <header className={styles.header}><div><span className={styles.eyebrow}>ORDER</span><h1>새 주문 등록</h1><p>수주 시점의 거래처·품목 정보를 주문에 스냅샷으로 보관합니다.</p></div></header>
      <form className={styles.form} onSubmit={submit}>
        <CatalogOrderSection
          clientId={clientId}
          productId={productId}
          onClientIdChangeAction={setClientId}
          onProductIdChangeAction={setProductId}
        />
        <OrderDetailsSection
          quantity={quantity}
          dueDate={dueDate}
          deliveryAddress={deliveryAddress}
          vatIncluded={vatIncluded}
          note={note}
          onQuantityChange={setQuantity}
          onDueDateChange={setDueDate}
          onDeliveryAddressChange={setDeliveryAddress}
          onVatIncludedChange={setVatIncluded}
          onNoteChange={setNote}
        />
        {error && <p className={styles.error}>{error}</p>}
        <div className={styles.actions}><button type="button" className={styles.secondary} onClick={() => router.back()}>취소</button><button disabled={saving}>{saving ? "등록 중..." : "주문 등록"}</button></div>
      </form>
    </main>
  );
}
