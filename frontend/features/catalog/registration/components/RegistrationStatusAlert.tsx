import type { RegistrationStatus } from "../types/clientRegistration";
import styles from "./ClientRegistration.module.css";

interface RegistrationStatusAlertProps {
  status: RegistrationStatus | null;
  rejectReason: string | null;
}

export function RegistrationStatusAlert({ status, rejectReason }: RegistrationStatusAlertProps) {
  if (status === "PENDING") {
    return (
      <div className={`${styles.alert} ${styles.alertPending}`}>
        <p className={styles.alertTitle}>⏳ 심사 중이에요</p>
        <p>제출하신 신청서를 관리자가 검토하고 있어요. 내용을 수정해서 다시 제출할 수 있습니다.</p>
      </div>
    );
  }

  if (status === "REJECTED") {
    return (
      <div className={`${styles.alert} ${styles.alertRejected}`}>
        <p className={styles.alertTitle}>반려되었어요</p>
        <p>{rejectReason ?? "반려 사유가 등록되지 않았습니다."}</p>
        <p className={styles.alertHint}>내용을 수정한 뒤 다시 제출해 주세요.</p>
      </div>
    );
  }

  if (status === "APPROVED") {
    return (
      <div className={`${styles.alert} ${styles.alertApproved}`}>
        <p className={styles.alertTitle}>승인되었어요</p>
        <p>정식 거래처로 등록되었습니다. 잠시 후 전체 메뉴로 전환됩니다.</p>
      </div>
    );
  }

  return (
    <div className={styles.emptyNotice}>
      <span className={styles.emptyIcon}>🏢</span>
      <div>
        <p className={styles.emptyTitle}>아직 등록 신청 전이에요</p>
        <p className={styles.emptyBody}>
          회사 정보를 입력하고 등록을 신청해 주세요. 관리자 승인 후 정식 거래처 메뉴가 열립니다.
        </p>
      </div>
    </div>
  );
}