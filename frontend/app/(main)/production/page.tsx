import styles from "./page.module.css";

export default function ProductionPage() {
  return (
    <div className={styles.shell}>
      <h1 className={styles.title}>생산 관리</h1>
      <p className={styles.placeholder}>생산 현황 (구현 예정)</p>
    </div>
  );
}
