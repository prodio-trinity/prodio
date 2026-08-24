import Link from "next/link";
import styles from "./Pending.module.css";

const PRODUCTS = [
  {
    badge: "품목 카탈로그",
    name: "필요한 품목을 간편하게",
    description: "다양한 품목을 카탈로그에서 확인하고 원하는 품목을 선택해 주문할 수 있습니다.",
  },
  {
    badge: "생산 관리",
    name: "주문부터 납품까지 한눈에",
    description: "주문한 품목의 생산 진행 상황과 납품 상태를 단계별로 확인하고, 상태가 변경될 때마다 알림을 받을 수 있습니다.",
  },
  {
    badge: "배송 관리",
    name: "배송까지 편리하게",
    description: "자주 사용하는 배송지를 등록해두고 주문할 때마다 간편하게 선택할 수 있습니다.",
  },
];

export function PendingHome() {
  return <div className={styles.page}>
    <section className={styles.hero}>
      <div className={styles.heroCopy}>
        <span className={styles.eyebrow}>PRODIO FOR BUSINESS</span>
        <h1>생산 주문을 더 간단하고 정확하게</h1>
        <p>거래처 등록 후 품목별 주문, 생산 진행 확인, 배송 정보 관리를 이용할 수 있습니다.</p>
      </div>
      <div className={styles.heroAction}><Link className={styles.primary} href="/pending/registration">거래처 등록 신청</Link></div>
    </section>
    <section className={styles.section}>
      <h2>Prodio 주요 서비스</h2>
      <div className={styles.products}>{PRODUCTS.map((product) => <article className={styles.product} key={product.name}>
        <span className={styles.productBadge}>{product.badge}</span><h3>{product.name}</h3><p>{product.description}</p>
      </article>)}</div>
    </section>
  </div>;
}
