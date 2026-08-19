"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { orderService } from "../services/orderService";
import type { DeliveryAddress, DeliveryContext } from "../types/order";
import styles from "./orders.module.css";

type DaumPostcodeResult = {
  zonecode: string;
  roadAddress: string;
  jibunAddress: string;
  buildingName: string;
  apartment: "Y" | "N";
};

declare global {
  interface Window {
    daum?: { Postcode: new (options: { oncomplete: (data: DaumPostcodeResult) => void }) => {
      embed: (element: HTMLElement) => void;
    } };
  }
}

function formatKoreanPhone(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 11);
  if (digits.startsWith("02")) {
    if (digits.length <= 2) return digits;
    if (digits.length <= 5) return `${digits.slice(0, 2)}-${digits.slice(2)}`;
    if (digits.length <= 9) return `${digits.slice(0, 2)}-${digits.slice(2, 5)}-${digits.slice(5)}`;
    return `${digits.slice(0, 2)}-${digits.slice(2, 6)}-${digits.slice(6)}`;
  }
  if (digits.length <= 3) return digits;
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  if (digits.length <= 10) return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
}

function phoneInputParts(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 11);
  const firstLength = digits.startsWith("02") ? 2 : 3;
  const remaining = digits.slice(firstLength);
  const middleLength = digits.startsWith("02") && digits.length <= 9 ? 3
    : remaining.length > 7 ? 4 : Math.min(4, remaining.length);
  return [digits.slice(0, firstLength), remaining.slice(0, middleLength), remaining.slice(middleLength)];
}

function loadPostcodeScript() {
  if (window.daum?.Postcode) return Promise.resolve();
  return new Promise<void>((resolve, reject) => {
    const existing = document.querySelector<HTMLScriptElement>("script[data-daum-postcode]");
    if (existing) {
      existing.addEventListener("load", () => resolve(), { once: true });
      existing.addEventListener("error", () => reject(new Error("주소 검색 서비스를 불러오지 못했습니다.")), { once: true });
      return;
    }
    const script = document.createElement("script");
    script.src = "https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
    script.async = true;
    script.dataset.daumPostcode = "true";
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("주소 검색 서비스를 불러오지 못했습니다."));
    document.head.appendChild(script);
  });
}

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
  const [addressSearchOpen, setAddressSearchOpen] = useState(false);
  const [selectedPreset, setSelectedPreset] = useState<"recent" | "headquarters" | null>(null);
  const postcodeContainerRef = useRef<HTMLDivElement>(null);
  const phoneMiddleRef = useRef<HTMLInputElement>(null);
  const phoneLastRef = useRef<HTMLInputElement>(null);
  const phoneParts = phoneInputParts(draft.recipientPhone);

  const load = useCallback(() => {
    if (!clientId) return Promise.resolve();
    return orderService.getDeliveryContext(String(clientId))
      .then((result) => {
        setContext(result); setError("");
        if (!delivery && result.headquarters) {
          onDeliveryChangeAction(result.headquarters);
          setSelectedPreset("headquarters");
        }
      })
      .catch((cause: unknown) => setError(cause instanceof Error
        ? cause.message : "배송 정보를 불러오지 못했습니다."));
  }, [clientId, delivery, onDeliveryChangeAction]);

  useEffect(() => { void load(); }, [load]);

  useEffect(() => {
    if (!addressSearchOpen) return;
    let cancelled = false;
    void loadPostcodeScript().then(() => {
      if (cancelled || !window.daum?.Postcode || !postcodeContainerRef.current) return;
      postcodeContainerRef.current.replaceChildren();
      new window.daum.Postcode({
        oncomplete: (data) => {
          const address = data.roadAddress || data.jibunAddress;
          const extra = data.roadAddress && data.buildingName
            ? ` (${data.buildingName}${data.apartment === "Y" ? ", 아파트" : ""})` : "";
          setDraft((current) => ({
            ...current,
            postalCode: data.zonecode,
            addressLine1: `${address}${extra}`,
          }));
          setAddressSearchOpen(false);
        },
      }).embed(postcodeContainerRef.current);
    }).catch((cause: unknown) => {
      if (!cancelled) setError(cause instanceof Error ? cause.message : "주소 검색을 시작하지 못했습니다.");
    });
    return () => { cancelled = true; };
  }, [addressSearchOpen]);

  function startCreate() { setEditingId(undefined); setDraft(EMPTY_DELIVERY); }
  function startEdit(address: DeliveryAddress) {
    setEditingId(address.addressId ?? undefined);
    setDraft({ ...address, recipientPhone: address.recipientPhone.replace(/\D/g, "") });
  }

  function changePhonePart(index: number, value: string) {
    const next = [...phoneParts];
    const maxLength = index === 0 ? 3 : 4;
    next[index] = value.replace(/\D/g, "").slice(0, maxLength);
    setDraft({ ...draft, recipientPhone: next.join("") });
    if (next[index].length === maxLength) {
      if (index === 0) phoneMiddleRef.current?.focus();
      if (index === 1) phoneLastRef.current?.focus();
    }
  }

  async function save() {
    if (!clientId || !draft.name.trim() || !draft.addressLine1.trim()) {
      return setError("배송지명과 주소를 입력해 주세요.");
    }
    setSaving(true); setError("");
    try {
      const payload = { ...draft, recipientPhone: formatKoreanPhone(draft.recipientPhone) };
      const saved = editingId
        ? await orderService.updateDeliveryAddress(editingId, payload)
        : await orderService.createDeliveryAddress(String(clientId), payload);
      setSelectedPreset(null);
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

  function searchAddress() { setError(""); setAddressSearchOpen(true); }

  return <section className={styles.card}>
    <div className={styles.sectionTitle}><h2>2. 배송 정보</h2><div className={styles.deliveryButtons}>
      <button type="button" className={selectedPreset === "recent" ? styles.deliveryPresetActive : undefined} disabled={!context?.recent} onClick={() => { setSelectedPreset("recent"); onDeliveryChangeAction(context?.recent ?? undefined); }}>최근 배송지</button>
      <button type="button" className={selectedPreset === "headquarters" ? styles.deliveryPresetActive : undefined} disabled={!context?.headquarters} onClick={() => { setSelectedPreset("headquarters"); onDeliveryChangeAction(context?.headquarters ?? undefined); }}>본사</button>
      <button type="button" disabled={!clientId} onClick={() => setOpen(true)}>배송지 관리</button>
    </div></div>
    {!clientId && <p className={styles.emptyInline}>수주처를 먼저 선택해 주세요.</p>}
    {error && !open && <p className={styles.error}>{error}</p>}
    {delivery && <div className={styles.deliveryPreview}>
      <strong>{delivery.name}</strong><span>{delivery.recipientName || "-"} · {formatKoreanPhone(delivery.recipientPhone) || "-"}</span>
      <p>{delivery.postalCode} {delivery.addressLine1} {delivery.addressLine2}</p>
    </div>}
    {open && <div className={styles.modalBackdrop} role="presentation" onMouseDown={() => setOpen(false)}>
      <section className={styles.deliveryModal} role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}>
        <div className={styles.sectionTitle}><div><h2>배송지 관리</h2><p>거래처의 주문용 배송지를 등록하고 관리합니다.</p></div><button type="button" onClick={() => setOpen(false)}>닫기</button></div>
        {error && <p className={styles.error}>{error}</p>}
        <div className={styles.addressList}>
          {context?.savedAddresses.length ? context.savedAddresses.map((address) => <article key={address.addressId}>
            <button type="button" className={styles.addressSelect} onClick={() => { setSelectedPreset(null); onDeliveryChangeAction(address); setOpen(false); }}><strong>{address.name}</strong><span>{address.addressLine1} {address.addressLine2}</span></button>
            <div><button type="button" onClick={() => startEdit(address)}>수정</button><button type="button" disabled={saving} onClick={() => void remove(address)}>삭제</button></div>
          </article>) : <p className={styles.emptyInline}>등록된 배송지가 없습니다.</p>}
        </div>
        <div className={styles.deliveryForm}>
          <h3>{editingId ? "배송지 수정" : "새로운 배송지 등록"}</h3>
          <div className={styles.grid}>
            <label>배송지명<input value={draft.name} onChange={(event) => setDraft({ ...draft, name: event.target.value })} placeholder="예: 제2공장" /></label>
            <label>받는 분<input value={draft.recipientName} onChange={(event) => setDraft({ ...draft, recipientName: event.target.value })} /></label>
            <label>연락처<div className={styles.phoneInputGroup}>
              <input inputMode="numeric" aria-label="연락처 앞자리" maxLength={3} value={phoneParts[0]} placeholder="010" onChange={(event) => changePhonePart(0, event.target.value)} />
              <span>-</span>
              <input ref={phoneMiddleRef} inputMode="numeric" aria-label="연락처 중간자리" maxLength={4} value={phoneParts[1]} placeholder="0000" onChange={(event) => changePhonePart(1, event.target.value)} />
              <span>-</span>
              <input ref={phoneLastRef} inputMode="numeric" aria-label="연락처 끝자리" maxLength={4} value={phoneParts[2]} placeholder="0000" onChange={(event) => changePhonePart(2, event.target.value)} />
            </div></label>
            <label>우편번호<div className={styles.addressSearchRow}><input value={draft.postalCode} readOnly placeholder="주소 검색으로 입력" /><button type="button" onClick={searchAddress}>주소 검색</button></div></label>
            <label className={styles.wide}>주소<input value={draft.addressLine1} readOnly placeholder="주소 검색 버튼을 눌러 입력" /></label>
            <label className={styles.wide}>상세 주소<input value={draft.addressLine2} onChange={(event) => setDraft({ ...draft, addressLine2: event.target.value })} /></label>
          </div>
          {addressSearchOpen && <div className={styles.postcodeBackdrop} role="presentation" onMouseDown={() => setAddressSearchOpen(false)}>
            <section className={styles.postcodeDialog} role="dialog" aria-modal="true" aria-label="주소 검색" onMouseDown={(event) => event.stopPropagation()}>
              <div><div><strong>주소 검색</strong><p>도로명 또는 지번 주소를 검색하세요.</p></div><button type="button" onClick={() => setAddressSearchOpen(false)}>닫기</button></div>
              <div ref={postcodeContainerRef} className={styles.postcodeEmbed} />
            </section>
          </div>}
          <div className={styles.actions}>{editingId && <button type="button" className={styles.secondary} onClick={startCreate}>신규 등록으로 전환</button>}<button type="button" disabled={saving} onClick={() => void save()}>{saving ? "저장 중..." : editingId ? "수정 저장" : "배송지 등록"}</button></div>
        </div>
      </section>
    </div>}
  </section>;
}
