"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { orderService } from "../services/orderService";
import { ORDER_STATUS_LABELS, type OrderPage, type OrderStatus } from "../types/order";
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
  return <main className={styles.shell}>
    <header className={styles.header}><div><span className={styles.eyebrow}>ORDER</span><h1>주문 관리</h1><p>접수된 주문의 내용, 입금 확정과 취소를 관리합니다.</p></div><Link className={styles.primaryLink} href="/orders/new">+ 새 주문</Link></header>
    <section className={styles.summary}><div><span>전체 주문</span><strong>{data.totalElements}</strong></div><div><span>입금 대기</span><strong>{data.orders.filter(o => o.status === "PENDING_PAYMENT").length}</strong></div><div><span>주문 확정</span><strong>{data.orders.filter(o => o.status === "CONFIRMED").length}</strong></div></section>
    <section className={styles.card}>
      <div className={styles.filters}><input value={query} onChange={e => { setQuery(e.target.value); setPage(0); }} placeholder="거래처 또는 품목 검색" /><select value={status} onChange={e => { setStatus(e.target.value as OrderStatus | ""); setPage(0); }}><option value="">전체 상태</option><option value="PENDING_PAYMENT">입금 대기</option><option value="CONFIRMED">주문 확정</option><option value="CANCELLED">주문 취소</option></select></div>
      {error && <p className={styles.error}>{error}</p>}{loading ? <p className={styles.empty}>불러오는 중...</p> : data.orders.length === 0 ? <p className={styles.empty}>등록된 주문이 없습니다.</p> :
      <div className={styles.tableWrap}><table><thead><tr><th>주문번호</th><th>수주처 / 품목</th><th>수량</th><th>금액</th><th>납기</th><th>상태</th></tr></thead><tbody>{data.orders.map(order => <tr key={order.id}><td><Link href={`/orders/${order.id}`}>#{order.id}</Link></td><td><strong>{order.clientName}</strong><span>{order.items.map((item) => item.productName).join(", ")}</span></td><td>{order.items.reduce((sum, item) => sum + item.quantity, 0).toLocaleString()}</td><td>{order.totalAmount.toLocaleString()}원</td><td>{order.dueDate}</td><td><span className={styles.badge}>{ORDER_STATUS_LABELS[order.status]}</span></td></tr>)}</tbody></table></div>}
      <div className={styles.pagination}><button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button><span>{data.totalPages === 0 ? 0 : page + 1} / {data.totalPages}</span><button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>다음</button></div>
    </section>
  </main>;
}
