"use client";

import { useState, type FormEvent } from "react";
import type { MyRegistration, RegistrationSubmitRequest } from "../types/clientRegistration";
import {
  hasRegistrationErrors,
  validateRegistration,
  type RegistrationFormErrors,
} from "../utils/validateRegistration";
import styles from "./ClientRegistration.module.css";

interface RegistrationFormProps {
  initial: MyRegistration | null;
  submitting: boolean;
  error: string;
  onSubmit: (request: RegistrationSubmitRequest) => void;
}

export function RegistrationForm({ initial, submitting, error, onSubmit }: RegistrationFormProps) {
  const [form, setForm] = useState<RegistrationSubmitRequest>({
    companyName: initial?.companyName ?? "",
    ceoName: initial?.ceoName ?? "",
    businessRegNo: initial?.businessRegNo ?? "",
    phone: initial?.phone ?? "",
    address: initial?.address ?? "",
    managerName: initial?.managerName ?? "",
  });
  const [fieldErrors, setFieldErrors] = useState<RegistrationFormErrors>({});

  const isResubmit = initial !== null;

  function update<K extends keyof RegistrationSubmitRequest>(key: K, value: string) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const errors = validateRegistration(form);
    if (hasRegistrationErrors(errors)) {
      setFieldErrors(errors);
      return;
    }
    setFieldErrors({});
    onSubmit(form);
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <h2 className={styles.sectionTitle}>사업자 정보</h2>
      <div className={styles.grid}>
        <label>
          <span className={styles.labelText}>
            사업자등록번호<span className={styles.required}>*</span>
          </span>
          <input
            required
            placeholder="000-00-00000"
            pattern="\d{3}-\d{2}-\d{5}"
            title="000-00-00000 형식으로 입력해 주세요."
            value={form.businessRegNo}
            onChange={(event) => update("businessRegNo", event.target.value)}
          />
          {fieldErrors.businessRegNo && <p className={styles.fieldError}>{fieldErrors.businessRegNo}</p>}
        </label>
        <label>
          <span className={styles.labelText}>
            회사명<span className={styles.required}>*</span>
          </span>
          <input
            required
            placeholder="예: (주)프로디오"
            value={form.companyName}
            onChange={(event) => update("companyName", event.target.value)}
          />
          {fieldErrors.companyName && <p className={styles.fieldError}>{fieldErrors.companyName}</p>}
        </label>
        <label>
          대표자
          <input
            placeholder="대표자명"
            value={form.ceoName}
            onChange={(event) => update("ceoName", event.target.value)}
          />
        </label>
        <label className={styles.wide}>
          주소
          <input
            placeholder="회사 주소"
            value={form.address}
            onChange={(event) => update("address", event.target.value)}
          />
        </label>
      </div>

      <h2 className={styles.sectionTitle}>담당자 정보</h2>
      <div className={styles.grid}>
        <label>
          담당자명
          <input
            placeholder="담당자명"
            value={form.managerName}
            onChange={(event) => update("managerName", event.target.value)}
          />
        </label>
        <label>
          연락처
          <input
            placeholder="010-0000-0000"
            pattern="\d{2,3}-\d{3,4}-\d{4}"
            title="하이픈(-)을 포함해 입력해 주세요."
            value={form.phone}
            onChange={(event) => update("phone", event.target.value)}
          />
          {fieldErrors.phone && <p className={styles.fieldError}>{fieldErrors.phone}</p>}
        </label>
      </div>

      {error && <p className={styles.error}>{error}</p>}

      <div className={styles.actions}>
        <button type="submit" className={styles.primary} disabled={submitting}>
          {submitting ? "제출 중..." : isResubmit ? "수정 후 재제출" : "등록 신청"}
        </button>
      </div>
    </form>
  );
}