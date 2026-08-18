import styles from "./page.module.css";

export default function MyOrdersPage() {
  return (
    <div className={styles.shell}>
      <h1 className={styles.title}>내 수주 현황</h1>
      <p className={styles.placeholder}>수주 목록 (구현 예정)</p>
    </div>
  );
}
