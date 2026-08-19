import Link from "next/link";
import styles from "./ClientOnboarding.module.css";

const PRODUCTS = [
  { badge: "정밀 가공", name: "맞춤형 금속 부품", description: "도면과 요구 규격에 맞춘 정밀 부품 생산을 지원합니다." },
  { badge: "소량 생산", name: "시제품 제작", description: "개발 단계의 시제품부터 반복 생산까지 유연하게 대응합니다." },
  { badge: "품질 관리", name: "생산 이력 관리", description: "주문부터 생산·납품까지 진행 이력을 한곳에서 확인할 수 있습니다." },
];

export function UnregisteredClientHome() {
  return <div className={styles.page}>
    <section className={styles.notice}>
      <strong>거래처 등록이 필요합니다.</strong>
      <p>주문과 납품 서비스를 이용하려면 먼저 사업자 정보를 등록해 주세요.</p>
    </section>
    <section className={styles.hero}>
      <div className={styles.heroCopy}>
        <span className={styles.eyebrow}>PRODIO FOR BUSINESS</span>
        <h1>생산 주문을 더 간단하고 정확하게</h1>
        <p>거래처 등록 후 품목별 주문, 생산 진행 확인, 배송 정보 관리를 이용할 수 있습니다.</p>
      </div>
      <div className={styles.heroAction}><Link className={styles.primary} href="/client-registration">거래처 등록 정보 입력</Link></div>
    </section>
    <section className={styles.section}>
      <h2>Prodio 주요 서비스</h2>
      <div className={styles.products}>{PRODUCTS.map((product) => <article className={styles.product} key={product.name}>
        <span className={styles.productBadge}>{product.badge}</span><h3>{product.name}</h3><p>{product.description}</p>
      </article>)}</div>
    </section>
  </div>;
}
