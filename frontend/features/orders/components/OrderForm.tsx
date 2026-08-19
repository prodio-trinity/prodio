"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { CatalogOrderSection } from "@/features/catalog/components/order-selection/CatalogOrderSection";
import { orderService } from "../services/orderService";
import type { Order } from "../types/order";
import { OrderDetailsSection } from "./OrderDetailsSection";
import styles from "./orders.module.css";

export function OrderForm({ initialOrder }: { initialOrder?: Order }) {
  const router = useRouter();
  const editing = initialOrder !== undefined;
  const [clientId, setClientId] = useState<number | undefined>(() => initialOrder ? Number(initialOrder.clientId) : undefined);
  const [productId, setProductId] = useState<number | undefined>(() => initialOrder ? Number(initialOrder.productId) : undefined);
  const [quantity, setQuantity] = useState(initialOrder?.quantity ?? 1);
  const [vatIncluded, setVatIncluded] = useState(initialOrder?.vatIncluded ?? true);
  const [dueDate, setDueDate] = useState(initialOrder?.dueDate ?? "");
  const [deliveryAddress, setDeliveryAddress] = useState(initialOrder?.deliveryAddress ?? "");
  const [note, setNote] = useState(initialOrder?.note ?? "");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!clientId || !productId) return setError("수주처와 품목을 선택해 주세요.");
    setSaving(true); setError("");
    try {
      const details = { productId: String(productId), quantity, vatIncluded, dueDate, deliveryAddress, note };
      const order = editing
        ? await orderService.update(initialOrder.id, details)
        : await orderService.create({ clientId: String(clientId), ...details });
      router.push(`/orders/${order.id}`);
    } catch (cause) { setError(cause instanceof Error ? cause.message : `주문 ${editing ? "수정" : "등록"}에 실패했습니다.`); }
    finally { setSaving(false); }
  }

  return (
    <main className={styles.shell}>
      <header className={styles.header}><div><span className={styles.eyebrow}>ORDER</span><h1>{editing ? `주문 #${initialOrder.id} 수정` : "새 주문 등록"}</h1><p>{editing ? "거래처는 유지하고 품목과 주문 내용을 변경합니다." : "수주 시점의 거래처·품목 정보를 주문에 스냅샷으로 보관합니다."}</p></div></header>
      <form className={styles.form} onSubmit={submit}>
        <CatalogOrderSection
          clientId={clientId}
          productId={productId}
          clientReadOnly={editing}
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
        <div className={styles.actions}><button type="button" className={styles.secondary} onClick={() => router.back()}>취소</button><button disabled={saving}>{saving ? "저장 중..." : editing ? "변경 저장" : "주문 등록"}</button></div>
      </form>
    </main>
  );
}

export function OrderEditForm({ id }: { id: string }) {
  const [order, setOrder] = useState<Order>();
  const [error, setError] = useState("");

  useEffect(() => {
    void orderService.get(id)
      .then((result) => {
        if (result.status !== "PENDING_PAYMENT") {
          throw new Error("입금 대기 상태의 주문만 수정할 수 있습니다.");
        }
        setOrder(result);
      })
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "주문을 불러오지 못했습니다."));
  }, [id]);

  if (error) return <main className={styles.shell}><p className={styles.error}>{error}</p></main>;
  if (!order) return <main className={styles.shell}><p className={styles.empty}>불러오는 중...</p></main>;
  return <OrderForm initialOrder={order} />;
}
