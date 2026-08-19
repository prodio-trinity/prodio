"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { orderService } from "../services/orderService";
import { ORDER_STATUS_LABELS, type Order } from "../types/order";
import styles from "./orders.module.css";

export function MyOrderDetail({ id }: { id: string }) {
  const [order, setOrder] = useState<Order>();
  const [error, setError] = useState("");
  const load = useCallback(() => orderService.getMine(id)
    .then((result) => { setOrder(result); setError(""); })
    .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "주문을 불러오지 못했습니다.")), [id]);

  useEffect(() => { void load(); }, [load]);

  if (error && !order) return <main className={styles.shell}><p className={styles.error}>{error}</p><Link href="/my-orders">목록으로</Link></main>;
  if (!order) return <main className={styles.shell}><p className={styles.empty}>불러오는 중...</p></main>;

  return <main className={styles.shell}>
    <header className={styles.header}><div><Link className={styles.back} href="/my-orders">← 내 주문 현황</Link><h1>주문 #{order.id}</h1><p>{order.items.length}개 품목 주문의 진행 상황입니다.</p></div>{order.status === "PENDING_PAYMENT" && <div className={styles.headerActions}><Link className={styles.secondary} href={`/my-orders/${order.id}/edit`}>주문 내용 수정</Link></div>}</header>
    <section className={styles.progressGrid}>
      <article className={styles.progressItem}><span>주문</span><strong>{ORDER_STATUS_LABELS[order.status]}</strong></article>
      <article className={styles.progressItem}><span>생산</span><strong>생산 정보 연동 전</strong></article>
      <article className={styles.progressItem}><span>납품</span><strong>납품 정보 연동 전</strong></article>
      <article className={styles.progressItem}><span>결제</span><strong>{order.status === "CONFIRMED" ? "결제 확인" : order.status === "CANCELLED" ? "주문 취소" : "미확인"}</strong></article>
    </section>
    <section className={styles.detailGrid}>
      <article className={styles.card}><h2>주문 정보</h2><dl className={styles.details}><div><dt>거래처</dt><dd>{order.clientName}</dd></div>{order.items.map((item) => <div key={item.productId}><dt>{item.productName}</dt><dd>{item.quantity.toLocaleString()}개 · {item.lineAmount.toLocaleString()}원</dd></div>)}<div><dt>총액</dt><dd><strong>{order.totalAmount.toLocaleString()}원</strong></dd></div></dl></article>
      <article className={styles.card}><h2>납기 및 결제</h2><dl className={styles.details}><div><dt>납기일</dt><dd>{order.dueDate}</dd></div><div><dt>배송지</dt><dd>{order.delivery.name}</dd></div><div><dt>배송 주소</dt><dd>{order.delivery.postalCode} {order.delivery.addressLine1} {order.delivery.addressLine2}</dd></div><div><dt>결제 상태</dt><dd>{order.status === "CONFIRMED" ? "확인" : order.status === "CANCELLED" ? "취소" : "미확인"}</dd></div><div><dt>등록일</dt><dd>{new Date(order.createdAt).toLocaleString("ko-KR")}</dd></div>{order.cancellationReason && <div><dt>취소 사유</dt><dd>{order.cancellationReason}</dd></div>}</dl></article>
      <article className={`${styles.card} ${styles.full}`}><h2>요청 메모</h2><p>{order.note || "-"}</p></article>
    </section>
  </main>;
}
