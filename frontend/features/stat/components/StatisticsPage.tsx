"use client";

import { useStatFilters } from "../hooks/useStatFilters";
import { AiSummarySection } from "./AiSummarySection";
import { DashboardSection } from "./DashboardSection";
import { RagQaSection } from "./RagQaSection";
import styles from "./StatisticsPage.module.css";

export function StatisticsPage() {
  const { draft, setDraft, filters, submit, applyPreset } = useStatFilters();

  return (
    <div className={styles.shell}>
      <DashboardSection
        draft={draft}
        onDraftChange={setDraft}
        filters={filters}
        onSubmit={submit}
        onApplyPreset={applyPreset}
      />

      <AiSummarySection filters={filters} />

      <RagQaSection />
    </div>
  );
}
