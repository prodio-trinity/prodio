import styles from "./page.module.css";

export default function ClientsPage() {
  return (
    <div className={styles.shell}>
      <h1 className={styles.title}>클라이언트 관리</h1>
      <p className={styles.placeholder}>클라이언트 목록 (구현 예정)</p>
    </div>
  );
}
