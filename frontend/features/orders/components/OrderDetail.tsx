"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { orderService } from "../services/orderService";
import { ORDER_STATUS_LABELS, type Order } from "../types/order";
import styles from "./orders.module.css";

export function OrderDetail({ id }: { id: string }) {
  const [order, setOrder] = useState<Order>();
  const [error, setError] = useState("");
  const [working, setWorking] = useState(false);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState("");
  const load = useCallback(() => orderService.get(id)
    .then((result) => { setOrder(result); setError(""); })
    .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "주문을 불러오지 못했습니다.")), [id]);
  useEffect(() => { void load(); }, [load]);

  async function action(task: () => Promise<Order>) {
    setWorking(true); setError("");
    try { setOrder(await task()); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "요청에 실패했습니다."); }
    finally { setWorking(false); }
  }

  async function cancel() {
    if (!cancelReason.trim()) return setError("취소 사유를 입력해 주세요.");
    await action(() => orderService.cancel(id, cancelReason.trim()));
    setCancelOpen(false);
  }

  if (error && !order) return <main className={styles.shell}><p className={styles.error}>{error}</p><Link href="/orders">목록으로</Link></main>;
  if (!order) return <main className={styles.shell}><p className={styles.empty}>불러오는 중...</p></main>;
  const pending = order.status === "PENDING_PAYMENT";

  return <main className={styles.shell}>
    <header className={styles.header}>
      <div><Link className={styles.back} href="/orders">← 주문 목록</Link><h1>주문 #{order.id}</h1><p>{order.clientName}의 {order.items.length}개 품목 주문입니다.</p></div>
      {pending && <div className={styles.headerActions}>
        <Link className={styles.secondary} href={`/orders/${order.id}/edit`}>주문 내용 수정</Link>
        <button type="button" className={styles.secondary} disabled={working} onClick={() => setCancelOpen(true)}>주문 취소</button>
        <button type="button" disabled={working} onClick={() => void action(() => orderService.confirm(order.id))}>입금 완료</button>
      </div>}
    </header>
    {error && <p className={styles.error}>{error}</p>}
    {cancelOpen && <section className={styles.card}>
      <h2>주문 취소</h2>
      <label>취소 사유<textarea value={cancelReason} maxLength={1000} onChange={(event) => setCancelReason(event.target.value)} /></label>
      <div className={styles.actions}><button type="button" className={styles.secondary} onClick={() => setCancelOpen(false)}>닫기</button><button type="button" disabled={working} onClick={() => void cancel()}>취소 확정</button></div>
    </section>}
    <section className={styles.detailGrid}>
      <article className={styles.card}><h2>수주 정보</h2><dl className={styles.details}><div><dt>수주처</dt><dd>{order.clientName}</dd></div>{order.items.map((item) => <div key={item.productId}><dt>{item.productName}</dt><dd>{item.quantity.toLocaleString()}개 × {item.unitPrice.toLocaleString()}원 = {item.lineAmount.toLocaleString()}원</dd></div>)}<div><dt>부가세</dt><dd>{order.vatIncluded ? "포함" : "별도"}</dd></div><div><dt>총액</dt><dd><strong>{order.totalAmount.toLocaleString()}원</strong></dd></div></dl></article>
      <article className={styles.card}><h2>진행 정보</h2><dl className={styles.details}><div><dt>상태</dt><dd><span className={styles.badge}>{ORDER_STATUS_LABELS[order.status]}</span></dd></div><div><dt>납기일</dt><dd>{order.dueDate}</dd></div><div><dt>등록자 ID</dt><dd>{order.createdBy}</dd></div><div><dt>등록일</dt><dd>{new Date(order.createdAt).toLocaleString("ko-KR")}</dd></div>{order.cancellationReason && <div><dt>취소 사유</dt><dd>{order.cancellationReason}</dd></div>}</dl></article>
      <article className={`${styles.card} ${styles.full}`}><h2>배송 및 메모</h2><dl className={styles.details}><div><dt>배송지</dt><dd>{order.delivery.name}</dd></div><div><dt>수령인</dt><dd>{order.delivery.recipientName || "-"} {order.delivery.recipientPhone}</dd></div><div><dt>배송 주소</dt><dd>{order.delivery.postalCode} {order.delivery.addressLine1} {order.delivery.addressLine2}</dd></div><div><dt>메모</dt><dd>{order.note || "-"}</dd></div></dl></article>
    </section>
  </main>;
}
