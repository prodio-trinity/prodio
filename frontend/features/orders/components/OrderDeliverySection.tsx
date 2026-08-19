"use client";

import { useCallback, useEffect, useState } from "react";
import { orderService } from "../services/orderService";
import type { DeliveryAddress, DeliveryContext } from "../types/order";
import styles from "./orders.module.css";

const EMPTY_DELIVERY: DeliveryAddress = {
  addressId: null, name: "", recipientName: "", recipientPhone: "",
  postalCode: "", addressLine1: "", addressLine2: "",
};

export function OrderDeliverySection({ clientId, delivery, onDeliveryChangeAction }: {
  clientId?: number;
  delivery?: DeliveryAddress;
  onDeliveryChangeAction: (delivery?: DeliveryAddress) => void;
}) {
  const [context, setContext] = useState<DeliveryContext>();
  const [error, setError] = useState("");
  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState<string>();
  const [draft, setDraft] = useState<DeliveryAddress>(EMPTY_DELIVERY);
  const [saving, setSaving] = useState(false);

  const load = useCallback(() => {
    if (!clientId) return Promise.resolve();
    return orderService.getDeliveryContext(String(clientId))
      .then((result) => {
        setContext(result); setError("");
        if (!delivery && result.headquarters) onDeliveryChangeAction(result.headquarters);
      })
      .catch((cause: unknown) => setError(cause instanceof Error
        ? cause.message : "배송 정보를 불러오지 못했습니다."));
  }, [clientId, delivery, onDeliveryChangeAction]);

  useEffect(() => { void load(); }, [load]);

  function startCreate() { setEditingId(undefined); setDraft(EMPTY_DELIVERY); }
  function startEdit(address: DeliveryAddress) { setEditingId(address.addressId ?? undefined); setDraft(address); }

  async function save() {
    if (!clientId || !draft.name.trim() || !draft.addressLine1.trim()) {
      return setError("배송지명과 주소를 입력해 주세요.");
    }
    setSaving(true); setError("");
    try {
      const saved = editingId
        ? await orderService.updateDeliveryAddress(editingId, draft)
        : await orderService.createDeliveryAddress(String(clientId), draft);
      onDeliveryChangeAction(saved);
      startCreate();
      await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : "배송지를 저장하지 못했습니다."); }
    finally { setSaving(false); }
  }

  async function remove(address: DeliveryAddress) {
    if (!address.addressId) return;
    setSaving(true); setError("");
    try {
      await orderService.deleteDeliveryAddress(address.addressId);
      if (delivery?.addressId === address.addressId) onDeliveryChangeAction(undefined);
      await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : "배송지를 삭제하지 못했습니다."); }
    finally { setSaving(false); }
  }

  return <section className={styles.card}>
    <div className={styles.sectionTitle}><h2>3. 배송 정보</h2><div className={styles.deliveryButtons}>
      <button type="button" disabled={!context?.recent} onClick={() => onDeliveryChangeAction(context?.recent ?? undefined)}>최근 배송지</button>
      <button type="button" disabled={!context?.headquarters} onClick={() => onDeliveryChangeAction(context?.headquarters ?? undefined)}>본사</button>
      <button type="button" disabled={!clientId} onClick={() => setOpen(true)}>배송지 관리</button>
    </div></div>
    {!clientId && <p className={styles.emptyInline}>수주처를 먼저 선택해 주세요.</p>}
    {error && <p className={styles.error}>{error}</p>}
    {delivery && <div className={styles.deliveryPreview}>
      <strong>{delivery.name}</strong><span>{delivery.recipientName} {delivery.recipientPhone}</span>
      <p>{delivery.postalCode} {delivery.addressLine1} {delivery.addressLine2}</p>
    </div>}
    {open && <div className={styles.modalBackdrop} role="presentation" onMouseDown={() => setOpen(false)}>
      <section className={styles.deliveryModal} role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}>
        <div className={styles.sectionTitle}><div><h2>배송지 관리</h2><p>거래처의 주문용 배송지를 등록하고 관리합니다.</p></div><button type="button" onClick={() => setOpen(false)}>닫기</button></div>
        <div className={styles.addressList}>
          {context?.savedAddresses.length ? context.savedAddresses.map((address) => <article key={address.addressId}>
            <button type="button" className={styles.addressSelect} onClick={() => { onDeliveryChangeAction(address); setOpen(false); }}><strong>{address.name}</strong><span>{address.addressLine1} {address.addressLine2}</span></button>
            <div><button type="button" onClick={() => startEdit(address)}>수정</button><button type="button" disabled={saving} onClick={() => void remove(address)}>삭제</button></div>
          </article>) : <p className={styles.emptyInline}>등록된 배송지가 없습니다.</p>}
        </div>
        <div className={styles.deliveryForm}>
          <h3>{editingId ? "배송지 수정" : "새로운 배송지 등록"}</h3>
          <div className={styles.grid}>
            <label>배송지명<input value={draft.name} onChange={(event) => setDraft({ ...draft, name: event.target.value })} placeholder="예: 제2공장" /></label>
            <label>받는 분<input value={draft.recipientName} onChange={(event) => setDraft({ ...draft, recipientName: event.target.value })} /></label>
            <label>연락처<input value={draft.recipientPhone} onChange={(event) => setDraft({ ...draft, recipientPhone: event.target.value })} /></label>
            <label>우편번호<input value={draft.postalCode} onChange={(event) => setDraft({ ...draft, postalCode: event.target.value })} /></label>
            <label className={styles.wide}>주소<input value={draft.addressLine1} onChange={(event) => setDraft({ ...draft, addressLine1: event.target.value })} /></label>
            <label className={styles.wide}>상세 주소<input value={draft.addressLine2} onChange={(event) => setDraft({ ...draft, addressLine2: event.target.value })} /></label>
          </div>
          <div className={styles.actions}>{editingId && <button type="button" className={styles.secondary} onClick={startCreate}>신규 등록으로 전환</button>}<button type="button" disabled={saving} onClick={() => void save()}>{saving ? "저장 중..." : editingId ? "수정 저장" : "배송지 등록"}</button></div>
        </div>
      </section>
    </div>}
  </section>;
}
