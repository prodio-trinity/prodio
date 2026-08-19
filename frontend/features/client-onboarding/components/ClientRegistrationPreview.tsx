import styles from "./ClientOnboarding.module.css";

export function ClientRegistrationPreview() {
  return <div className={styles.page}>
    <section className={styles.formCard}>
      <header className={styles.formHeader}><span className={styles.eyebrow}>CLIENT REGISTRATION</span><h1>거래처 등록</h1><p>아래 화면은 등록에 필요한 정보를 확인하기 위한 임시 화면이며 아직 저장되지 않습니다.</p></header>
      <section className={styles.formSection}><h2>사업자 정보</h2><div className={styles.grid}>
        <label>사업자등록번호<input placeholder="000-00-00000" /></label>
        <label>회사명<input placeholder="주식회사 프로디오" /></label>
        <label>대표자명<input placeholder="홍길동" /></label>
        <label>업태·종목<input placeholder="제조업 / 금속가공" /></label>
        <label className={styles.wide}>사업장 주소<input placeholder="우편번호와 기본 주소" /></label>
        <label className={styles.wide}>상세 주소<input placeholder="상세 주소" /></label>
      </div></section>
      <section className={styles.formSection}><h2>담당자 정보</h2><div className={styles.grid}>
        <label>담당자명<input placeholder="담당자 이름" /></label>
        <label>연락처<input placeholder="010-0000-0000" /></label>
        <label>이메일<input type="email" placeholder="manager@example.com" /></label>
        <label>사업자등록증<input type="file" /></label>
        <label className={styles.wide}>요청 사항<textarea rows={4} placeholder="거래처 등록 시 참고할 내용을 입력하세요." /></label>
      </div></section>
      <div className={styles.actions}><button className={styles.disabled} type="button" disabled>등록 기능 준비 중</button></div>
    </section>
  </div>;
}
