import { X } from "lucide-react";
import gridStyles from "@/features/catalog/shared/components/AdminGrid.module.css";
import type { EditableClientRow } from "../types/client";
import type { EditableTextField } from "../utils/clientRow";
import styles from "./ClientList.module.css";

interface ClientGridTableProps {
  rows: EditableClientRow[];
  loading: boolean;
  onCellChange: (id: number, field: EditableTextField, value: string) => void;
  onToggleActive: (id: number, checked: boolean) => void;
  onRemoveNewRow: (id: number) => void;
}

const TEXT_COLUMNS: { field: EditableTextField; label: string; width: string; placeholder?: string }[] = [
  { field: "companyName", label: "회사명", width: "13%", placeholder: "회사명" },
  { field: "ceoName", label: "대표자", width: "8%" },
  { field: "businessRegNo", label: "사업자등록번호", width: "11%" },
  { field: "phone", label: "연락처", width: "10%" },
  { field: "address", label: "주소", width: "14%" },
  { field: "managerName", label: "담당자", width: "7%" },
  { field: "memo", label: "비고", width: "11%" },
];

function formatCreatedAt(value: string): string {
  if (!value) return "-";
  return new Date(value).toLocaleDateString("ko-KR");
}

export function ClientGridTable({ rows, loading, onCellChange, onToggleActive, onRemoveNewRow }: ClientGridTableProps) {
  if (loading) {
    return <p className={gridStyles.emptyRows}>불러오는 중...</p>;
  }
  if (rows.length === 0) {
    return <p className={gridStyles.emptyRows}>조회된 거래처가 없습니다.</p>;
  }

  return (
    <div className={gridStyles.tableWrap}>
      <table>
        <thead>
          <tr>
            <th style={{ width: "9%" }}>거래처코드</th>
            {TEXT_COLUMNS.map((col) => (
              <th key={col.field} style={{ width: col.width }}>
                {col.label}
              </th>
            ))}
            <th style={{ width: "8%" }}>계정연동</th>
            <th style={{ width: "8%" }}>등록일</th>
            <th className={gridStyles.activeCol}>사용여부</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => {
            const rowClass = row.error
              ? gridStyles.rowError
              : row.isNew
                ? gridStyles.rowNew
                : row.dirty
                  ? gridStyles.rowDirty
                  : "";
            return (
              <tr key={row.id} className={rowClass}>
                <td className={gridStyles.codeCell}>
                  {row.isNew ? "(자동생성)" : row.clientCode}
                  {row.isNew ? (
                    <>
                      <span className={gridStyles.newBadge}>NEW</span>
                      <button
                        type="button"
                        className={gridStyles.removeNewBtn}
                        title="이 행 삭제"
                        onClick={() => onRemoveNewRow(row.id)}
                      >
                        <X size={12} />
                      </button>
                    </>
                  ) : null}
                  {row.error ? <div className={gridStyles.rowErrorMsg}>{row.error}</div> : null}
                </td>
                {TEXT_COLUMNS.map((col) => (
                  <td key={col.field}>
                    <input
                      className={gridStyles.cellInput}
                      value={row[col.field]}
                      placeholder={col.placeholder}
                      onChange={(event) => onCellChange(row.id, col.field, event.target.value)}
                    />
                  </td>
                ))}
                <td>
                  {row.isNew ? (
                    <span className={`${styles.linkBadge} ${styles.linkBadgeNone}`}>-</span>
                  ) : (
                    <span
                      className={`${styles.linkBadge} ${row.linkedToAccount ? "" : styles.linkBadgeNone}`}
                      title="catalog_clients.user_id — 읽기 전용"
                    >
                      {row.linkedToAccount ? "연동됨" : "미연동"}
                    </span>
                  )}
                </td>
                <td className={gridStyles.mutedCell}>{row.isNew ? "-" : formatCreatedAt(row.createdAt)}</td>
                <td className={gridStyles.activeCol}>
                  <label
                    className={`${gridStyles.activeBadge} ${row.isActive ? gridStyles.activeBadgeOn : gridStyles.activeBadgeOff}`}
                  >
                    <input
                      type="checkbox"
                      checked={row.isActive}
                      onChange={(event) => onToggleActive(row.id, event.target.checked)}
                    />
                    {row.isActive ? "사용" : "미사용"}
                  </label>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}