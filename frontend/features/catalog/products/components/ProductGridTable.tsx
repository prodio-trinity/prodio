import { X } from "lucide-react";
import gridStyles from "@/features/catalog/shared/components/AdminGrid.module.css";
import type { CatalogSubCategory, CatalogTopCategory } from "../utils/product";
import type { EditableProductRow } from "../utils/productRow";

const UNIT_OPTIONS = ["EA", "BOX", "KG", "SET"];

interface ProductGridTableProps {
  rows: EditableProductRow[];
  loading: boolean;
  topCategories: CatalogTopCategory[];
  subCategories: CatalogSubCategory[];
  onProductNameChange: (id: number, value: string) => void;
  onCategoryChange: (id: number, subCategoryId: number | null) => void;
  onUnitChange: (id: number, unit: string) => void;
  onUnitPriceChange: (id: number, unitPrice: number) => void;
  onToggleActive: (id: number, checked: boolean) => void;
  onRemoveNewRow: (id: number) => void;
}

export function ProductGridTable({
  rows,
  loading,
  topCategories,
  subCategories,
  onProductNameChange,
  onCategoryChange,
  onUnitChange,
  onUnitPriceChange,
  onToggleActive,
  onRemoveNewRow,
}: ProductGridTableProps) {
  if (loading) {
    return <p className={gridStyles.emptyRows}>불러오는 중...</p>;
  }
  if (rows.length === 0) {
    return <p className={gridStyles.emptyRows}>조회된 품목이 없습니다.</p>;
  }

  return (
    <div className={gridStyles.tableWrap}>
      <table>
        <thead>
          <tr>
            <th style={{ width: "12%" }}>품목코드</th>
            <th style={{ width: "24%" }}>품목명</th>
            <th style={{ width: "20%" }}>분류</th>
            <th style={{ width: "10%" }}>단위</th>
            <th style={{ width: "14%" }}>단가</th>
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
                  {row.isNew ? "(자동생성)" : row.productCode}
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
                <td>
                  <input
                    className={gridStyles.cellInput}
                    value={row.productName}
                    placeholder="품목명"
                    onChange={(event) => onProductNameChange(row.id, event.target.value)}
                  />
                </td>
                <td>
                  <select
                    className={gridStyles.cellInput}
                    value={row.subCategoryId ?? ""}
                    onChange={(event) => onCategoryChange(row.id, event.target.value ? Number(event.target.value) : null)}
                  >
                    <option value="">선택</option>
                    {topCategories.map((top) => {
                      const subs = subCategories.filter((sub) => sub.topCategory === top.code);
                      if (subs.length === 0) return null;
                      return (
                        <optgroup key={top.code} label={top.displayName}>
                          {subs.map((sub) => (
                            <option key={sub.id} value={sub.id}>
                              {sub.name}
                            </option>
                          ))}
                        </optgroup>
                      );
                    })}
                  </select>
                </td>
                <td>
                  <select
                    className={gridStyles.cellInput}
                    value={row.unit}
                    onChange={(event) => onUnitChange(row.id, event.target.value)}
                  >
                    {UNIT_OPTIONS.map((unit) => (
                      <option key={unit} value={unit}>
                        {unit}
                      </option>
                    ))}
                  </select>
                </td>
                <td>
                  <input
                    type="number"
                    min={0}
                    className={gridStyles.cellInput}
                    style={{ textAlign: "right" }}
                    value={row.unitPrice}
                    onChange={(event) => onUnitPriceChange(row.id, Number(event.target.value))}
                  />
                </td>
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
