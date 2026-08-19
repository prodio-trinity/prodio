"use client";

import type { OrderClientContext, OrderProductContext } from "../types/order";
import type { OrderItemSelection } from "./OrderCatalogSection";
import styles from "./orders.module.css";

type QuotationItem = {
  code: string;
  name: string;
  unit: string;
  quantity: number;
  unitPrice: number;
  amount: number;
};

function currency(value: number) {
  return `${value.toLocaleString("ko-KR")}원`;
}

function quotationItems(products: OrderProductContext[], items: OrderItemSelection[]) {
  return items.map((item) => {
    const product = products.find((candidate) => Number(candidate.productId) === item.productId);
    const unitPrice = product?.unitPrice ?? 0;
    return {
      code: product?.productCode ?? `#${item.productId}`,
      name: product?.name ?? `품목 #${item.productId}`,
      unit: product?.unit ?? "EA",
      quantity: item.quantity,
      unitPrice,
      amount: unitPrice * item.quantity,
    } satisfies QuotationItem;
  });
}

function saveQuotationPng(client: OrderClientContext, items: QuotationItem[], vatIncluded: boolean) {
  // A4 세로 비율(210:297)의 고해상도 PNG 캔버스
  const width = 1400;
  const height = 1980;
  const tableHeight = Math.min(920, 58 * (items.length + 1));
  const rowHeight = Math.max(24, Math.floor(tableHeight / (items.length + 1)));
  const canvas = document.createElement("canvas");
  canvas.width = width;
  canvas.height = height;
  const context = canvas.getContext("2d");
  if (!context) return;

  const subtotal = items.reduce((sum, item) => sum + item.amount, 0);
  const vat = vatIncluded ? Math.round(subtotal * 0.1) : 0;
  const total = subtotal + vat;
  const left = 80;
  const right = width - 80;
  const columns = [left, 190, 650, 800, 960, 1160, right];
  const line = (x1: number, y1: number, x2: number, y2: number) => {
    context.beginPath(); context.moveTo(x1, y1); context.lineTo(x2, y2); context.stroke();
  };
  const text = (value: string, x: number, y: number, align: CanvasTextAlign = "left") => {
    context.textAlign = align; context.fillText(value, x, y);
  };

  context.fillStyle = "#ffffff";
  context.fillRect(0, 0, width, height);
  context.fillStyle = "#111827";
  context.strokeStyle = "#cbd5e1";
  context.lineWidth = 2;
  context.font = "700 48px sans-serif";
  context.textAlign = "center";
  context.fillText("견 적 서", width / 2, 90);
  context.font = "24px sans-serif";
  context.textAlign = "left";
  context.fillText(`${client.companyName} 귀중`, left, 160);
  context.font = "20px sans-serif";
  context.fillStyle = "#475569";
  context.fillText(`견적일: ${new Date().toLocaleDateString("ko-KR")}`, left, 205);
  context.fillText(`사업자번호: ${client.businessRegistrationNumber || "-"}`, left, 240);
  context.textAlign = "right";
  context.fillText("공급자: PRODIO", right, 160);
  context.fillText("아래와 같이 견적합니다.", right, 205);

  const tableTop = 290;
  context.fillStyle = "#eef2ff";
  context.fillRect(left, tableTop, right - left, rowHeight);
  context.fillStyle = "#111827";
  context.font = `700 ${Math.max(11, Math.min(19, Math.floor(rowHeight * 0.38)))}px sans-serif`;
  ["번호", "품목", "단위", "수량", "단가", "금액"].forEach((label, index) => {
    text(label, (columns[index] + columns[index + 1]) / 2,
      tableTop + Math.floor(rowHeight * 0.65), "center");
  });
  for (let index = 0; index <= columns.length - 1; index += 1) line(columns[index], tableTop, columns[index], tableTop + rowHeight * (items.length + 1));
  for (let index = 0; index <= items.length + 1; index += 1) line(left, tableTop + rowHeight * index, right, tableTop + rowHeight * index);

  context.font = `${Math.max(11, Math.min(18, Math.floor(rowHeight * 0.36)))}px sans-serif`;
  items.forEach((item, index) => {
    const y = tableTop + rowHeight * (index + 1) + Math.floor(rowHeight * 0.65);
    text(String(index + 1), (columns[0] + columns[1]) / 2, y, "center");
    text(`${item.name} (${item.code})`, columns[1] + 14, y);
    text(item.unit, (columns[2] + columns[3]) / 2, y, "center");
    text(item.quantity.toLocaleString("ko-KR"), columns[4] - 14, y, "right");
    text(currency(item.unitPrice), columns[5] - 14, y, "right");
    text(currency(item.amount), columns[6] - 14, y, "right");
  });

  const summaryTop = Math.min(1540, tableTop + rowHeight * (items.length + 1) + 70);
  context.font = "20px sans-serif";
  text("공급가액", 1040, summaryTop, "right");
  text(currency(subtotal), right, summaryTop, "right");
  text("부가세", 1040, summaryTop + 42, "right");
  text(currency(vat), right, summaryTop + 42, "right");
  context.font = "700 28px sans-serif";
  text("합계", 1040, summaryTop + 94, "right");
  text(currency(total), right, summaryTop + 94, "right");
  context.font = "17px sans-serif";
  context.fillStyle = "#64748b";
  line(left, height - 135, right, height - 135);
  text("본 견적서는 주문 등록 전 확인용이며, 실제 계약 조건은 주문 확정 시 결정됩니다.", left, height - 90);

  canvas.toBlob((blob) => {
    if (!blob) return;
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = `견적서-A4-${new Date().toISOString().slice(0, 10)}.png`;
    anchor.click();
    URL.revokeObjectURL(url);
  }, "image/png");
}

export function OrderQuotationModal({ client, products, items, vatIncluded, onCloseAction }: {
  client: OrderClientContext;
  products: OrderProductContext[];
  items: OrderItemSelection[];
  vatIncluded: boolean;
  onCloseAction: () => void;
}) {
  const quoteItems = quotationItems(products, items);
  const subtotal = quoteItems.reduce((sum, item) => sum + item.amount, 0);
  const vat = vatIncluded ? Math.round(subtotal * 0.1) : 0;
  const total = subtotal + vat;

  function printQuotation() {
    const originalTitle = document.title;
    document.title = `견적서-${client.companyName}`;
    window.print();
    document.title = originalTitle;
  }

  return <div className={styles.modalBackdrop} role="presentation" onMouseDown={onCloseAction}>
    <section className={styles.quotationModal} role="dialog" aria-modal="true" aria-label="견적서" onMouseDown={(event) => event.stopPropagation()}>
      <div className={styles.quotePrintArea}>
        <header className={styles.quoteHeader}><div><span>QUOTATION</span><h2>견 적 서</h2></div><div><strong>PRODIO</strong><span>견적일 {new Date().toLocaleDateString("ko-KR")}</span></div></header>
        <section className={styles.quoteClient}><strong>{client.companyName} 귀중</strong><dl>
          <div><dt>사업자번호</dt><dd>{client.businessRegistrationNumber || "-"}</dd></div>
          <div><dt>대표자</dt><dd>{client.representative || "-"}</dd></div>
          <div><dt>담당자</dt><dd>{client.managerName || "-"}</dd></div>
          <div><dt>연락처</dt><dd>{client.phone || "-"}</dd></div>
        </dl></section>
        <p className={styles.quoteIntro}>아래와 같이 견적합니다.</p>
        <div className={styles.quoteTableWrap}><table className={styles.quoteTable}><thead><tr><th>번호</th><th>품목</th><th>단위</th><th>수량</th><th>단가</th><th>금액</th></tr></thead><tbody>
          {quoteItems.map((item, index) => <tr key={`${item.code}-${index}`}><td>{index + 1}</td><td><strong>{item.name}</strong><span>{item.code}</span></td><td>{item.unit}</td><td>{item.quantity.toLocaleString("ko-KR")}</td><td>{currency(item.unitPrice)}</td><td>{currency(item.amount)}</td></tr>)}
        </tbody></table></div>
        <dl className={styles.quoteSummary}><div><dt>공급가액</dt><dd>{currency(subtotal)}</dd></div><div><dt>부가세</dt><dd>{currency(vat)}</dd></div><div><dt>합계</dt><dd>{currency(total)}</dd></div></dl>
        <p className={styles.quoteNotice}>본 견적서는 주문 등록 전 확인용이며, 실제 계약 조건은 주문 확정 시 결정됩니다.</p>
      </div>
      <footer className={styles.quoteModalActions}>
        <div><button type="button" onClick={() => saveQuotationPng(client, quoteItems, vatIncluded)}>사진 파일 저장</button><button type="button" onClick={printQuotation}>인쇄</button><button type="button" className={styles.secondary} onClick={onCloseAction}>닫기</button></div>
      </footer>
    </section>
  </div>;
}
