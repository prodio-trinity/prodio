import type { SelectedProduct } from "../utils/product";
import styles from "./ProductPicker.module.css";

interface SelectedProductTableProps {
  items: SelectedProduct[];
  onQuantityChange: (productId: number, quantity: number) => void;
  onRemove: (productId: number) => void;
}

export function SelectedProductTable({ items, onQuantityChange, onRemove }: SelectedProductTableProps) {
  if (items.length === 0) {
    return <p className={styles.emptyItems}>선택된 품목이 없습니다.</p>;
  }

  const subtotal = items.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);

  return (
    <>
      <table className={styles.itemsTable}>
        <thead>
          <tr>
            <th>품목</th>
            <th>단가</th>
            <th>수량</th>
            <th>소계</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.productId}>
              <td>
                <strong>{item.productName}</strong>
                <span>
                  {item.productCode} · {item.categoryDisplayName}
                </span>
              </td>
              <td>{item.unitPrice.toLocaleString("ko-KR")}원</td>
              <td>
                <input
                  type="number"
                  min={1}
                  value={item.quantity}
                  onChange={(event) => onQuantityChange(item.productId, Number(event.target.value))}
                />
              </td>
              <td>{(item.unitPrice * item.quantity).toLocaleString("ko-KR")}원</td>
              <td>
                <button type="button" onClick={() => onRemove(item.productId)}>
                  삭제
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className={styles.totalRow}>
        <span>합계</span>
        <strong>{subtotal.toLocaleString("ko-KR")}원</strong>
      </div>
    </>
  );
}