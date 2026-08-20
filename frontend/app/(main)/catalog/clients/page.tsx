import Link from "next/link";
import styles from "./page.module.css";

export default function ClientsPage() {
  return (
    <div className={styles.shell}>
      <h1 className={styles.title}>클라이언트 관리</h1>
      <p className={styles.placeholder}>클라이언트 목록 (구현 예정)</p>
      <p>
        <Link href="/catalog/registration">거래처 등록 신청 관리 →</Link>
      </p>
    </div>
  );
}
