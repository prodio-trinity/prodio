"use client";

import { useCallback, useEffect, useState } from "react";
import { clientRegistrationService } from "../services/clientRegistrationService";
import type { MyRegistration, RegistrationSubmitRequest } from "../types/clientRegistration";
import { RegistrationForm } from "./RegistrationForm";
import { RegistrationStatusAlert } from "./RegistrationStatusAlert";
import styles from "./ClientRegistration.module.css";

export function ClientRegistrationPage() {
  const [data, setData] = useState<MyRegistration | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const load = useCallback(
    () =>
      clientRegistrationService
        .getMy()
        .then((result) => {
          setData(result);
          setLoadError("");
        })
        .catch((cause: unknown) =>
          setLoadError(cause instanceof Error ? cause.message : "등록 상태를 불러오지 못했습니다."),
        )
        .finally(() => setLoading(false)),
    [],
  );

  useEffect(() => {
    void load();
  }, [load]);

  async function handleSubmit(request: RegistrationSubmitRequest) {
    setSubmitting(true);
    setSubmitError("");
    try {
      const result = await clientRegistrationService.submit(request);
      setData(result);
    } catch (cause) {
      setSubmitError(cause instanceof Error ? cause.message : "등록 신청에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <main className={styles.shell}>
        <p className={styles.loading}>불러오는 중...</p>
      </main>
    );
  }

  const status = data?.registered ? data.status : null;

  return (
    <main className={styles.shell}>
      <header className={styles.header}>
        <span className={styles.eyebrow}>CLIENT REGISTRATION</span>
        <h1>거래처 등록 신청</h1>
      </header>

      {loadError && <p className={styles.error}>{loadError}</p>}

      <RegistrationStatusAlert status={status} rejectReason={data?.rejectReason ?? null} />

      {status !== "APPROVED" && (
        <section className={styles.card}>
          <RegistrationForm
            initial={data?.registered ? data : null}
            submitting={submitting}
            error={submitError}
            onSubmit={(request) => void handleSubmit(request)}
          />
        </section>
      )}
    </main>
  );
}