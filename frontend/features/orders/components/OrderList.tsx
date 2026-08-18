"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { orderService } from "../services/orderService";
import type { Order, OrderPage, OrderStatus } from "../types/order";
import styles from "./orders.module.css";

const EMPTY: OrderPage = { orders: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
export function OrderList() {
  const [data, setData] = useState(EMPTY); const [status, setStatus] = useState<OrderStatus | "">(""); const [query, setQuery] = useState("");
  const [page, setPage] = useState(0); const [loading, setLoading] = useState(true); const [error, setError] = useState("");
  const load = useCallback(() => orderService.list({ status: status || undefined, q: query || undefined, page, size: 10 })
    .then((result) => { setData(result); setError(""); })
    .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "주문을 불러오지 못했습니다."))
    .finally(() => setLoading(false)), [status, query, page]);
  useEffect(() => { void load(); }, [load]);
  async function togglePayment(order: Order) { await orderService.updatePayment(order.id, !order.paymentConfirmed); await load(); }
  async function start(order: Order) { await orderService.startProduction(order.id); await load(); }
  return <main className={styles.shell}>
    <header className={styles.header}><div><span className={styles.eyebrow}>ORDER</span><h1>주문 관리</h1><p>접수된 주문과 생산 전환, 입금 상태를 한곳에서 관리합니다.</p></div><Link className={styles.primaryLink} href="/orders/new">+ 새 주문</Link></header>
    <section className={styles.summary}><div><span>전체 주문</span><strong>{data.totalElements}</strong></div><div><span>현재 페이지</span><strong>{data.orders.length}</strong></div><div><span>생산 대기</span><strong>{data.orders.filter(o => o.status === "PENDING").length}</strong></div></section>
    <section className={styles.card}>
      <div className={styles.filters}><input value={query} onChange={e => { setQuery(e.target.value); setPage(0); }} placeholder="거래처 또는 품목 검색" /><select value={status} onChange={e => { setStatus(e.target.value as OrderStatus | ""); setPage(0); }}><option value="">전체 상태</option><option value="PENDING">생산 대기</option><option value="IN_PRODUCTION">생산 진행</option></select></div>
      {error && <p className={styles.error}>{error}</p>}{loading ? <p className={styles.empty}>불러오는 중...</p> : data.orders.length === 0 ? <p className={styles.empty}>등록된 주문이 없습니다.</p> :
      <div className={styles.tableWrap}><table><thead><tr><th>주문번호</th><th>수주처 / 품목</th><th>수량</th><th>금액</th><th>납기</th><th>상태</th><th>입금</th><th></th></tr></thead><tbody>{data.orders.map(order => <tr key={order.id}><td><Link href={`/orders/${order.id}`}>#{order.id}</Link></td><td><strong>{order.clientName}</strong><span>{order.productName}</span></td><td>{order.quantity.toLocaleString()}</td><td>{order.totalAmount.toLocaleString()}원</td><td>{order.dueDate}</td><td><span className={styles.badge}>{order.status === "PENDING" ? "생산 대기" : "생산 진행"}</span></td><td><button type="button" className={styles.textButton} onClick={() => void togglePayment(order)}>{order.paymentConfirmed ? "확인" : "미확인"}</button></td><td>{order.status === "PENDING" && <button type="button" className={styles.smallButton} onClick={() => void start(order)}>생산 시작</button>}</td></tr>)}</tbody></table></div>}
      <div className={styles.pagination}><button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button><span>{data.totalPages === 0 ? 0 : page + 1} / {data.totalPages}</span><button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>다음</button></div>
    </section>
  </main>;
}
