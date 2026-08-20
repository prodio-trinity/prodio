import type { RegistrationStatus } from "../types/clientRegistration";
import type { RegistrationRequest } from "../types/registrationRequest";
import styles from "./RegistrationRequestList.module.css";

interface RequestTableProps {
  requests: RegistrationRequest[];
  loading: boolean;
  onSelect: (request: RegistrationRequest) => void;
}

const STATUS_LABELS: Record<RegistrationStatus, string> = {
  PENDING: "대기",
  APPROVED: "승인",
  REJECTED: "반려",
};

function formatDate(value: string) {
  return new Date(value).toLocaleString("ko-KR", { dateStyle: "short", timeStyle: "short" });
}

export function RequestTable({ requests, loading, onSelect }: RequestTableProps) {
  if (loading) {
    return <p className={styles.empty}>불러오는 중...</p>;
  }

  if (requests.length === 0) {
    return <p className={styles.empty}>조회된 신청이 없습니다.</p>;
  }

  return (
    <div className={styles.tableWrap}>
      <table>
        <thead>
          <tr>
            <th>신청자(계정)</th>
            <th>회사명</th>
            <th>대표자</th>
            <th>사업자등록번호</th>
            <th>신청일</th>
            <th>상태</th>
          </tr>
        </thead>
        <tbody>
          {requests.map((request) => (
            <tr key={request.id} className={styles.row} onClick={() => onSelect(request)}>
              <td>{request.userEmail}</td>
              <td>{request.companyName}</td>
              <td>{request.ceoName ?? "-"}</td>
              <td className={styles.mono}>{request.businessRegNo ?? "-"}</td>
              <td className={styles.muted}>{formatDate(request.createdAt)}</td>
              <td>
                <span className={`${styles.badge} ${styles[`badge${request.status}`]}`}>
                  {STATUS_LABELS[request.status]}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}