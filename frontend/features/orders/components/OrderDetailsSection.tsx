import styles from "./orders.module.css";

export function OrderMemoSection({ note, onNoteChange }: {
  note: string;
  onNoteChange: (note: string) => void;
}) {
  return <section className={styles.card}>
    <h2>3. 메모</h2>
    <textarea rows={4} value={note} onChange={(event) => onNoteChange(event.target.value)} placeholder="생산 또는 납품 시 참고할 내용을 입력하세요." />
  </section>;
}
