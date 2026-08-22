import styles from "./page.module.css";

export default function StatPage() {
  return (
    <div className={styles.shell}>
      <h1 className={styles.title}>통계</h1>
      <p className={styles.placeholder}>통계 대시보드 + AI 요약 (구현 예정)</p>
    </div>
  );
}
