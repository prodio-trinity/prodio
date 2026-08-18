"use client";

import { useMemo, useState } from "react";
import type { ClientOption, ProductOption } from "../../types/order";
import styles from "./DummyCatalogSelectors.module.css";

export const DUMMY_CLIENTS: ClientOption[] = [
  { id: 1, name: "ABC 제조", contact: "김담당 · 010-1234-5678" },
  { id: 2, name: "한빛 테크", contact: "이담당 · 010-2345-6789" },
  { id: 3, name: "미래 정밀", contact: "박담당 · 010-3456-7890" },
];

export const DUMMY_PRODUCTS: ProductOption[] = [
  { id: 1, name: "표준 브래킷", unitPrice: 10000, specification: "SB-100 / 일반형" },
  { id: 2, name: "A형 부품", unitPrice: 15000, specification: "PART-A / 알루미늄" },
  { id: 3, name: "정밀 샤프트", unitPrice: 8500, specification: "SHAFT-08 / Ø8" },
];

type ClientSelectorProps = {
  value?: number;
  onChange: (client: ClientOption) => void;
};

type ProductSelectorProps = {
  value?: number;
  onChange: (product: ProductOption) => void;
};

function PreviewModal({ kind, onClose }: { kind: "거래처" | "품목"; onClose: () => void }) {
  return (
    <div className={styles.backdrop} role="presentation" onMouseDown={onClose}>
      <section className={styles.modal} role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}>
        <div>
          <span className={styles.previewBadge}>UI PREVIEW</span>
          <h3>미등록 {kind} 빠른 등록</h3>
          <p>향후 카탈로그 도메인이 제공할 등록 화면의 예상 모습입니다.</p>
        </div>
        <label>
          {kind}명
          <input disabled placeholder={`${kind}명을 입력하세요`} />
        </label>
        <label>
          {kind === "거래처" ? "담당자 연락처" : "규격 / 단가"}
          <input disabled placeholder="카탈로그 연동 후 사용할 수 있습니다" />
        </label>
        <div className={styles.modalActions}>
          <button type="button" onClick={onClose}>닫기</button>
          <button type="button" disabled>카탈로그에 등록</button>
        </div>
      </section>
    </div>
  );
}

export function ClientSelector({ value, onChange }: ClientSelectorProps) {
  const [query, setQuery] = useState("");
  const [previewOpen, setPreviewOpen] = useState(false);
  const options = useMemo(
    () => DUMMY_CLIENTS.filter((client) => client.name.toLowerCase().includes(query.toLowerCase())),
    [query],
  );

  return (
    <div className={styles.selector}>
      <div className={styles.selectorHeader}>
        <div><strong>수주처</strong><span>카탈로그 더미 데이터</span></div>
        <button type="button" onClick={() => setPreviewOpen(true)}>+ 미등록 거래처</button>
      </div>
      <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="거래처명으로 검색" />
      <div className={styles.options}>
        {options.map((client) => (
          <button
            type="button"
            className={value === client.id ? styles.selected : undefined}
            key={client.id}
            onClick={() => onChange(client)}
          >
            <strong>{client.name}</strong><span>{client.contact}</span>
          </button>
        ))}
      </div>
      {previewOpen && <PreviewModal kind="거래처" onClose={() => setPreviewOpen(false)} />}
    </div>
  );
}

export function ProductSelector({ value, onChange }: ProductSelectorProps) {
  const [query, setQuery] = useState("");
  const [previewOpen, setPreviewOpen] = useState(false);
  const options = useMemo(
    () => DUMMY_PRODUCTS.filter((product) => product.name.toLowerCase().includes(query.toLowerCase())),
    [query],
  );

  return (
    <div className={styles.selector}>
      <div className={styles.selectorHeader}>
        <div><strong>품목</strong><span>카탈로그 더미 데이터</span></div>
        <button type="button" onClick={() => setPreviewOpen(true)}>+ 미등록 품목</button>
      </div>
      <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="품목명으로 검색" />
      <div className={styles.options}>
        {options.map((product) => (
          <button
            type="button"
            className={value === product.id ? styles.selected : undefined}
            key={product.id}
            onClick={() => onChange(product)}
          >
            <strong>{product.name}</strong>
            <span>{product.specification} · {product.unitPrice.toLocaleString("ko-KR")}원</span>
          </button>
        ))}
      </div>
      {previewOpen && <PreviewModal kind="품목" onClose={() => setPreviewOpen(false)} />}
    </div>
  );
}
