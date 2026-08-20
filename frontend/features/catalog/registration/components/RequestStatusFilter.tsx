import type { RegistrationStatus } from "../types/clientRegistration";
import styles from "./RegistrationRequestList.module.css";

interface RequestStatusFilterProps {
  value: RegistrationStatus | "";
  onChange: (value: RegistrationStatus | "") => void;
}

const OPTIONS: { value: RegistrationStatus | ""; label: string }[] = [
  { value: "", label: "전체" },
  { value: "PENDING", label: "대기" },
  { value: "APPROVED", label: "승인" },
  { value: "REJECTED", label: "반려" },
];

export function RequestStatusFilter({ value, onChange }: RequestStatusFilterProps) {
  return (
    <label className={styles.filterField}>
      <span>상태</span>
      <select value={value} onChange={(event) => onChange(event.target.value as RegistrationStatus | "")}>
        {OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}