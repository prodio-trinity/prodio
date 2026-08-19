"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { orderService } from "../services/orderService";
import type { DeliveryAddress, Order, OrderFormContext } from "../types/order";
import { OrderCatalogSection } from "./OrderCatalogSection";
import { OrderMemoSection } from "./OrderDetailsSection";
import { OrderDeliverySection } from "./OrderDeliverySection";
import styles from "./orders.module.css";

export function OrderForm({ initialOrder, mine = false }: { initialOrder?: Order; mine?: boolean }) {
  const router = useRouter();
  const editing = initialOrder !== undefined;
  const [clientId, setClientId] = useState<number | undefined>(() => initialOrder ? Number(initialOrder.clientId) : undefined);
  const [formContext, setFormContext] = useState<OrderFormContext>();
  const [clientLoading, setClientLoading] = useState(true);
  const [clientLookupError, setClientLookupError] = useState("");
  const [items, setItems] = useState(() => initialOrder?.items.map((item) => ({ productId: Number(item.productId), quantity: item.quantity })) ?? []);
  const [orderName, setOrderName] = useState(initialOrder?.orderName ?? "");
  const [vatIncluded, setVatIncluded] = useState(initialOrder?.vatIncluded ?? true);
  const [delivery, setDelivery] = useState<DeliveryAddress | undefined>(() => initialOrder
    ? { ...initialOrder.delivery, addressId: null } : undefined);
  const [note, setNote] = useState(initialOrder?.note ?? "");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    void orderService.getFormContext(editing ? initialOrder.clientId : undefined)
      .then((result) => {
        setFormContext(result);
        setClientId(Number(result.client.clientId));
        setClientLookupError("");
      })
      .catch(() => setClientLookupError("거래처 등록이 필요합니다."))
      .finally(() => setClientLoading(false));
  }, [editing, initialOrder]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!clientId || items.length === 0) return setError("수주처와 품목을 선택해 주세요.");
    if (!delivery) return setError("배송 정보를 선택해 주세요.");
    setSaving(true); setError("");
    try {
      const details = { orderName: orderName.trim() || undefined, items: items.map((item) => ({ ...item, productId: String(item.productId) })), vatIncluded, delivery, note };
      const order = editing
        ? mine ? await orderService.updateMine(initialOrder.id, details) : await orderService.update(initialOrder.id, details)
        : await orderService.create({ clientId: String(clientId), ...details });
      router.push(editing && !mine ? `/orders/${order.id}` : `/my-orders/${order.id}`);
    } catch (cause) { setError(cause instanceof Error ? cause.message : `주문 ${editing ? "수정" : "등록"}에 실패했습니다.`); }
    finally { setSaving(false); }
  }

  if (clientLoading) return <main className={styles.shell}><p className={styles.empty}>거래처 정보를 확인하는 중...</p></main>;
  if (clientLookupError || !formContext) return <main className={styles.shell}><section className={styles.card}><h1>{editing ? "주문 수정" : "새 주문 등록"}</h1><p className={styles.error}>{clientLookupError || "수주 정보를 불러오지 못했습니다."}</p></section></main>;

  return (
    <main className={styles.shell}>
      <header className={styles.header}><div><span className={styles.eyebrow}>ORDER</span><h1>{editing ? `주문 #${initialOrder.id} 수정` : "새 주문 등록"}</h1><p>{editing ? "거래처는 유지하고 품목과 주문 내용을 변경합니다." : "수주 시점의 거래처·품목 정보를 주문에 스냅샷으로 보관합니다."}</p></div></header>
      <form className={styles.form} onSubmit={submit}>
        <OrderCatalogSection
          client={formContext.client}
          products={formContext.products}
          orderName={orderName}
          items={items}
          vatIncluded={vatIncluded}
          onItemsChangeAction={setItems}
          onOrderNameChangeAction={setOrderName}
          onVatIncludedChangeAction={setVatIncluded}
        />
        <OrderDeliverySection clientId={clientId} delivery={delivery} onDeliveryChangeAction={setDelivery} />
        <OrderMemoSection note={note} onNoteChange={setNote} />
        {error && <p className={styles.error}>{error}</p>}
        <div className={styles.actions}><button type="button" className={styles.secondary} onClick={() => router.back()}>취소</button><button disabled={saving}>{saving ? "저장 중..." : editing ? "변경 저장" : "주문 등록"}</button></div>
      </form>
    </main>
  );
}

export function OrderEditForm({ id, mine = false }: { id: string; mine?: boolean }) {
  const [order, setOrder] = useState<Order>();
  const [error, setError] = useState("");

  useEffect(() => {
    void (mine ? orderService.getMine(id) : orderService.get(id))
      .then((result) => {
        if (result.status !== "PENDING_PAYMENT") {
          throw new Error("입금 대기 상태의 주문만 수정할 수 있습니다.");
        }
        setOrder(result);
      })
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "주문을 불러오지 못했습니다."));
  }, [id, mine]);

  if (error) return <main className={styles.shell}><p className={styles.error}>{error}</p></main>;
  if (!order) return <main className={styles.shell}><p className={styles.empty}>불러오는 중...</p></main>;
  return <OrderForm initialOrder={order} mine={mine} />;
}
