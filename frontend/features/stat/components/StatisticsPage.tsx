"use client";

import { useStatFilters } from "../hooks/useStatFilters";
import { AiSummarySection } from "./AiSummarySection";
import { DashboardSection } from "./DashboardSection";
import { RagQaSection } from "./RagQaSection";
import styles from "./StatisticsPage.module.css";

export function StatisticsPage() {
  const { draft, setDraft, filters, submit } = useStatFilters();

  return (
    <div className={styles.shell}>
      <h1 className={styles.title}>통계</h1>

      <DashboardSection
        draft={draft}
        onDraftChange={setDraft}
        filters={filters}
        onSubmit={submit}
      />

      <hr className={styles.divider} />

      <AiSummarySection filters={filters} />

      <hr className={styles.divider} />

      <RagQaSection />
    </div>
  );
}
