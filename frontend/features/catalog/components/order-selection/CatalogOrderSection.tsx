"use client";

/**
 * [Catalog 담당자 구현 요청 요약]
 *
 * - 이 파일은 주문 등록 화면의 거래처·품목 선택 영역입니다.
 * - `DummyCatalogSelectors.tsx`와 `DummyCatalogSelectors.module.css`를 제거하고
 *   실제 Catalog 백엔드의 거래처·품목 조회 및 신규 등록 API를 연결해 주세요.
 * - 거래처를 선택하면 `onClientIdChangeAction(거래처 ID)`를 호출해 주세요.
 * - 품목을 선택하면 `onProductIdChangeAction(품목 ID)`를 호출해 주세요.
 * - 신규 등록 시 임시 값이 아니라 백엔드 저장 응답에 포함된 실제 ID를 전달해야 합니다.
 * - 거래처명·연락처·품목명·단가·규격과 검색 상태는 Catalog 컴포넌트 내부에서 관리합니다.
 * - Order는 전달받은 `clientId`, `productId`만 주문 등록 백엔드로 전송합니다.
 *
 * [파라미터]
 *
 * - `clientId?: number`
 *   현재 선택된 Catalog 거래처 ID입니다. `undefined`이면 아직 선택하지 않은 상태입니다.
 *
 * - `productId?: number`
 *   현재 선택된 Catalog 품목 ID입니다. `undefined`이면 아직 선택하지 않은 상태입니다.
 *
 * - `onClientIdChangeAction(clientId: number): void`
 *   기존 거래처 선택 또는 신규 거래처 저장이 완료되면 실제 거래처 ID만 전달합니다.
 *
 * - `onProductIdChangeAction(productId: number): void`
 *   기존 품목 선택 또는 신규 품목 저장이 완료되면 실제 품목 ID만 전달합니다.
 *
 * - `clientReadOnly?: boolean`
 *   `true`이면 기존 주문의 거래처를 표시만 하고 변경·신규 등록을 막습니다.
 */

import { ClientSelector, ProductSelector } from "./DummyCatalogSelectors";
import styles from "./CatalogOrderSection.module.css";

export type CatalogOrderSectionProps = {
  clientId?: number;
  productId?: number;
  clientReadOnly?: boolean;
  onClientIdChangeAction: (clientId: number) => void;
  onProductIdChangeAction: (productId: number) => void;
};

export function CatalogOrderSection({
  clientId,
  productId,
  clientReadOnly = false,
  onClientIdChangeAction,
  onProductIdChangeAction,
}: CatalogOrderSectionProps) {
  return (
    <section className={styles.card}>
      <h2>1. 수주 정보</h2>
      <ClientSelector
        value={clientId}
        readOnly={clientReadOnly}
        onChange={(client) => onClientIdChangeAction(client.id)}
      />
      <ProductSelector value={productId} onChange={(product) => onProductIdChangeAction(product.id)} />
    </section>
  );
}
