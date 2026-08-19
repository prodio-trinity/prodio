"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { orderService } from "../services/orderService";
import { ORDER_STATUS_LABELS, type Order, type OrderPage, type OrderStatus } from "../types/order";
import styles from "./orders.module.css";

const EMPTY: OrderPage = { orders: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };

export function MyOrderList() {
  const [data, setData] = useState(EMPTY);
  const [status, setStatus] = useState<OrderStatus | "">("");
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [summary, setSummary] = useState({ total: 0, pending: 0, confirmed: 0 });
  const [cancelOrder, setCancelOrder] = useState<Order>();
  const [cancelReason, setCancelReason] = useState("");
  const [cancelError, setCancelError] = useState("");
  const [cancelSaving, setCancelSaving] = useState(false);
  const load = useCallback(() =>
    orderService.listMine({ status: status || undefined, q: query || undefined, page, size: 10 })
      .then((result) => { setData(result); setError(""); })
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "주문을 불러오지 못했습니다."))
      .finally(() => setLoading(false)), [status, query, page]);

  useEffect(() => { void load(); }, [load]);
  const loadSummary = useCallback(() => Promise.all([
      orderService.listMine({ page: 0, size: 1 }),
      orderService.listMine({ status: "PENDING_PAYMENT", page: 0, size: 1 }),
      orderService.listMine({ status: "CONFIRMED", page: 0, size: 1 }),
    ]).then(([all, pending, confirmed]) => setSummary({
      total: all.totalElements,
      pending: pending.totalElements,
      confirmed: confirmed.totalElements,
    })), []);
  useEffect(() => { void loadSummary().catch(() => undefined); }, [loadSummary]);

  function openCancelModal(order: Order) {
    setCancelOrder(order); setCancelReason(""); setCancelError("");
  }

  async function cancelMine() {
    if (!cancelOrder) return;
    if (!cancelReason.trim()) return setCancelError("주문 취소 사유를 입력해 주세요.");
    setCancelSaving(true); setCancelError("");
    try {
      await orderService.cancelMine(cancelOrder.id, cancelReason.trim());
      setCancelOrder(undefined);
      await Promise.all([load(), loadSummary()]);
    } catch (cause) {
      setCancelError(cause instanceof Error ? cause.message : "주문을 취소하지 못했습니다.");
    } finally { setCancelSaving(false); }
  }

  return <main className={styles.shell}>
    <header className={styles.header}>
      <div><span className={styles.eyebrow}>MY ORDERS</span><h1>내 주문 현황</h1><p>주문별 진행 상태와 결제 여부를 확인합니다.</p></div>
      <Link className={styles.primaryLink} href="/orders/new">+ 새 주문 요청</Link>
    </header>
    <section className={styles.summary}>
      <div><span>전체 주문</span><strong>{summary.total}</strong></div>
      <div><span>입금 대기</span><strong>{summary.pending}</strong></div>
      <div><span>주문 확정</span><strong>{summary.confirmed}</strong></div>
    </section>
    <section className={styles.card}>
      <div className={styles.filters}>
        <input value={query} onChange={(event) => { setQuery(event.target.value); setPage(0); }} placeholder="거래처 또는 품목 검색" />
        <select value={status} onChange={(event) => { setStatus(event.target.value as OrderStatus | ""); setPage(0); }}>
          <option value="">전체 상태</option><option value="PENDING_PAYMENT">입금 대기</option><option value="CONFIRMED">주문 확정</option><option value="CANCELLED">주문 취소</option>
        </select>
      </div>
      {error && <p className={styles.error}>{error}</p>}
      {loading ? <p className={styles.empty}>불러오는 중...</p> : data.orders.length === 0 ? <p className={styles.empty}>등록한 주문이 없습니다.</p> :
        <div className={styles.tableWrap}><table><thead><tr><th>주문번호</th><th>품목</th><th>수량</th><th>주문 금액</th><th>진행 상태</th><th>작업</th></tr></thead>
          <tbody>{data.orders.map((order) => <tr key={order.id}>
            <td><Link href={`/my-orders/${order.id}`}>#{order.id}</Link></td><td><strong>{order.orderName || order.items.map((item) => item.productName).join(", ")}</strong><span>{order.clientName}{order.orderName ? ` · ${order.items.map((item) => item.productName).join(", ")}` : ""}</span></td>
            <td>{order.items.reduce((sum, item) => sum + item.quantity, 0).toLocaleString()}</td><td>{order.totalAmount.toLocaleString()}원</td>
            <td><span className={`${styles.badge} ${styles[`status${order.status}`]}`}>{ORDER_STATUS_LABELS[order.status]}</span></td><td>{order.status === "PENDING_PAYMENT" ? <div className={styles.rowActions}><Link className={styles.smallButton} href={`/my-orders/${order.id}/edit`}>수정</Link><button type="button" className={styles.smallDangerButton} onClick={() => openCancelModal(order)}>취소</button></div> : "-"}</td>
          </tr>)}</tbody></table></div>}
      <div className={styles.pagination}><button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button><span>{data.totalPages === 0 ? 0 : page + 1} / {data.totalPages}</span><button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>다음</button></div>
    </section>
    {cancelOrder && <div className={styles.modalBackdrop} role="presentation" onMouseDown={() => !cancelSaving && setCancelOrder(undefined)}>
      <section className={styles.statusModal} role="dialog" aria-modal="true" aria-label="주문 취소" onMouseDown={(event) => event.stopPropagation()}>
        <div><span className={styles.eyebrow}>ORDER CANCEL</span><h2>주문 취소</h2><p>주문 #{cancelOrder.id} · {cancelOrder.orderName || cancelOrder.items.map((item) => item.productName).join(", ")}</p></div>
        <label>취소 사유 <span>필수 입력</span><textarea rows={3} maxLength={1000} value={cancelReason} onChange={(event) => setCancelReason(event.target.value)} placeholder="취소 사유를 입력하세요." /></label>
        {cancelError && <p className={styles.error}>{cancelError}</p>}
        <div className={styles.statusModalActions}><button type="button" className={styles.secondary} disabled={cancelSaving} onClick={() => setCancelOrder(undefined)}>닫기</button><button type="button" className={styles.dangerButton} disabled={cancelSaving} onClick={() => void cancelMine()}>주문 취소</button></div>
      </section>
    </div>}
  </main>;
}
