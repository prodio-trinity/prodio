"use client";

import { useState } from "react";
import styles from "./RegistrationRequestList.module.css";

interface RejectReasonModalProps {
  companyName: string;
  submitting: boolean;
  error: string;
  onClose: () => void;
  onConfirm: (reason: string) => void;
}

export function RejectReasonModal({ companyName, submitting, error, onClose, onConfirm }: RejectReasonModalProps) {
  const [reason, setReason] = useState("");

  return (
    <div className={styles.modalBackdrop} role="presentation" onMouseDown={() => !submitting && onClose()}>
      <section
        className={styles.modal}
        style={{ width: "min(420px, 100%)" }}
        role="dialog"
        aria-modal="true"
        aria-label="신청 반려"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className={styles.modalHead}>
          <h2>신청 반려</h2>
          <button type="button" className={styles.modalClose} onClick={onClose} disabled={submitting}>
            ✕
          </button>
        </header>
        <div className={styles.modalBody}>
          <p className={styles.muted} style={{ marginBottom: 8 }}>
            {companyName}의 등록 신청을 반려합니다. 사유를 입력해 주세요.
          </p>
          <textarea
            className={styles.rejectTextarea}
            rows={4}
            value={reason}
            disabled={submitting}
            onChange={(event) => setReason(event.target.value)}
            placeholder="예: 이미 등록된 거래처의 사업자등록번호와 동일합니다."
          />
          {error && <p className={styles.error} style={{ marginTop: 10 }}>{error}</p>}
        </div>
        <footer className={styles.modalFoot}>
          <button type="button" className={styles.secondary} disabled={submitting} onClick={onClose}>
            취소
          </button>
          <button
            type="button"
            className={styles.danger}
            disabled={submitting || !reason.trim()}
            onClick={() => onConfirm(reason.trim())}
          >
            {submitting ? "반려 처리 중..." : "반려 확정"}
          </button>
        </footer>
      </section>
    </div>
  );
}