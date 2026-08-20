import type { RegistrationRequest } from "../types/registrationRequest";
import styles from "./RegistrationRequestList.module.css";

interface RequestDetailModalProps {
  request: RegistrationRequest;
  approving: boolean;
  approveError: string;
  onClose: () => void;
  onApprove: () => void;
  onReject: () => void;
}

function formatDate(value: string) {
  return new Date(value).toLocaleString("ko-KR", { dateStyle: "short", timeStyle: "short" });
}

export function RequestDetailModal({
  request,
  approving,
  approveError,
  onClose,
  onApprove,
  onReject,
}: RequestDetailModalProps) {
  return (
    <div className={styles.modalBackdrop} role="presentation" onMouseDown={() => !approving && onClose()}>
      <section
        className={styles.modal}
        role="dialog"
        aria-modal="true"
        aria-label="신청 상세"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className={styles.modalHead}>
          <h2>
            신청 상세 · {request.companyName}
            <span className={`${styles.badge} ${styles[`badge${request.status}`]}`}>
              {request.status}
            </span>
          </h2>
          <button type="button" className={styles.modalClose} onClick={onClose} disabled={approving}>
            ✕
          </button>
        </header>

        <div className={styles.modalBody}>
          <div className={styles.fieldGrid}>
            <div className={styles.fieldRow}>
              <label>신청자 계정</label>
              <div className={styles.value}>{request.userEmail}</div>
            </div>
            <div className={styles.fieldRow}>
              <label>신청일</label>
              <div className={styles.value}>{formatDate(request.createdAt)}</div>
            </div>
            <div className={styles.fieldRow}>
              <label>회사명</label>
              <div className={styles.value}>{request.companyName}</div>
            </div>
            <div className={styles.fieldRow}>
              <label>대표자</label>
              <div className={styles.value}>{request.ceoName ?? "-"}</div>
            </div>
            <div className={styles.fieldRow}>
              <label>사업자등록번호</label>
              <div className={styles.value}>{request.businessRegNo ?? "(미입력)"}</div>
            </div>
            <div className={styles.fieldRow}>
              <label>연락처</label>
              <div className={styles.value}>{request.phone ?? "-"}</div>
            </div>
            <div className={styles.fieldRow}>
              <label>주소</label>
              <div className={styles.value}>{request.address ?? "-"}</div>
            </div>
            <div className={styles.fieldRow}>
              <label>담당자</label>
              <div className={styles.value}>{request.managerName ?? "-"}</div>
            </div>
          </div>

          {request.status === "REJECTED" && request.rejectReason && (
            <div className={styles.rejectReasonBox}>반려 사유: {request.rejectReason}</div>
          )}
          {approveError && <p className={styles.error} style={{ marginTop: 14 }}>{approveError}</p>}
        </div>

        <footer className={styles.modalFoot}>
          {request.status === "PENDING" ? (
            <>
              <button type="button" className={styles.danger} disabled={approving} onClick={onReject}>
                반려
              </button>
              <button type="button" className={styles.primary} disabled={approving} onClick={onApprove}>
                {approving ? "승인 처리 중..." : "승인"}
              </button>
            </>
          ) : (
            <span className={styles.muted}>이미 처리된 신청입니다.</span>
          )}
        </footer>
      </section>
    </div>
  );
}