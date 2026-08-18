"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { orderService } from "../services/orderService";
import type { Order } from "../types/order";
import styles from "./orders.module.css";

export function OrderDetail({ id }: { id: string }) {
  const [order, setOrder] = useState<Order>();
  const [error, setError] = useState("");
  const [working, setWorking] = useState(false);
  const load = useCallback(() => orderService.get(id)
    .then((result) => { setOrder(result); setError(""); })
    .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "주문을 불러오지 못했습니다.")), [id]);
  useEffect(() => { void load(); }, [load]);
  async function action(task: () => Promise<Order>) {
    setWorking(true);
    try { setOrder(await task()); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "요청에 실패했습니다."); }
    finally { setWorking(false); }
  }
  if (error && !order) return <main className={styles.shell}><p className={styles.error}>{error}</p><Link href="/orders">목록으로</Link></main>;
  if (!order) return <main className={styles.shell}><p className={styles.empty}>불러오는 중...</p></main>;
  return <main className={styles.shell}>
    <header className={styles.header}><div><Link className={styles.back} href="/orders">← 주문 목록</Link><h1>주문 #{order.id}</h1><p>{order.clientName}의 {order.productName} 주문입니다.</p></div><div className={styles.headerActions}><button type="button" className={styles.secondary} disabled={working} onClick={() => void action(() => orderService.updatePayment(order.id, !order.paymentConfirmed))}>{order.paymentConfirmed ? "입금 확인 취소" : "입금 확인"}</button>{order.status === "PENDING" && <button type="button" disabled={working} onClick={() => void action(() => orderService.startProduction(order.id))}>생산 시작</button>}</div></header>
    {error && <p className={styles.error}>{error}</p>}
    <section className={styles.detailGrid}>
      <article className={styles.card}><h2>수주 정보</h2><dl className={styles.details}><div><dt>수주처</dt><dd>{order.clientName}</dd></div><div><dt>품목</dt><dd>{order.productName}</dd></div><div><dt>단가</dt><dd>{order.unitPrice.toLocaleString()}원</dd></div><div><dt>수량</dt><dd>{order.quantity.toLocaleString()}</dd></div><div><dt>부가세</dt><dd>{order.vatIncluded ? "포함" : "별도"}</dd></div><div><dt>총액</dt><dd><strong>{order.totalAmount.toLocaleString()}원</strong></dd></div></dl></article>
      <article className={styles.card}><h2>진행 정보</h2><dl className={styles.details}><div><dt>상태</dt><dd><span className={styles.badge}>{order.status === "PENDING" ? "생산 대기" : "생산 진행"}</span></dd></div><div><dt>입금</dt><dd>{order.paymentConfirmed ? "확인" : "미확인"}</dd></div><div><dt>납기일</dt><dd>{order.dueDate}</dd></div><div><dt>등록자 ID</dt><dd>{order.createdBy}</dd></div><div><dt>등록일</dt><dd>{new Date(order.createdAt).toLocaleString("ko-KR")}</dd></div></dl></article>
      <article className={`${styles.card} ${styles.full}`}><h2>납품 및 메모</h2><dl className={styles.details}><div><dt>납품 주소</dt><dd>{order.deliveryAddress || "-"}</dd></div><div><dt>메모</dt><dd>{order.note || "-"}</dd></div></dl></article>
    </section>
  </main>;
}
