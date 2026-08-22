"use client";

import { useAiSummary } from "../hooks/useAiSummary";
import { useStatFilters } from "../hooks/useStatFilters";
import { DashboardSection } from "./DashboardSection";
import { RagQaSection } from "./RagQaSection";
import styles from "./StatisticsPage.module.css";

export function StatisticsPage() {
  const { draft, setDraft, filters, submit, applyPreset } = useStatFilters();
  const aiSummary = useAiSummary(filters);

  return (
    <div className={styles.shell}>
      <DashboardSection
        draft={draft}
        onDraftChange={setDraft}
        filters={filters}
        onSubmit={submit}
        onApplyPreset={applyPreset}
        aiSummary={aiSummary}
      />

      <RagQaSection />
    </div>
  );
}
