import styles from "./page.module.css";

export default function DashboardPage() {
  return (
    <div className={styles.shell}>
      <h1 className={styles.title}>대시보드</h1>
      <p className={styles.placeholder}>수주 현황 칸반 보드 (구현 예정)</p>
    </div>
  );
}
