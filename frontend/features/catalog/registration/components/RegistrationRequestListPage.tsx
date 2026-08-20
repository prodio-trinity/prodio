"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { registrationRequestAdminService } from "../services/registrationRequestAdminService";
import type { RegistrationStatus } from "../types/clientRegistration";
import type { RegistrationRequest } from "../types/registrationRequest";
import { RegistrationRequestListView } from "./RegistrationRequestListView";
import { RequestDetailModal } from "./RequestDetailModal";
import { RejectReasonModal } from "./RejectReasonModal";

export function RegistrationRequestListPage() {
  const [status, setStatus] = useState<RegistrationStatus | "">("PENDING");
  const [items, setItems] = useState<RegistrationRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const [detailTarget, setDetailTarget] = useState<RegistrationRequest | null>(null);
  const [approving, setApproving] = useState(false);
  const [approveError, setApproveError] = useState("");
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [rejecting, setRejecting] = useState(false);
  const [rejectError, setRejectError] = useState("");

  const load = useCallback(
    () =>
      registrationRequestAdminService
        .list(status)
        .then((result) => {
          setItems(result);
          setLoadError("");
        })
        .catch((cause: unknown) => setLoadError(cause instanceof Error ? cause.message : "목록을 불러오지 못했습니다."))
        .finally(() => setLoading(false)),
    [status],
  );

  useEffect(() => {
    void load();
  }, [load]);

  const filtered = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase();
    if (!keyword) return items;
    return items.filter((item) =>
      [item.companyName, item.businessRegNo ?? "", item.userEmail].some((value) =>
        value.toLowerCase().includes(keyword),
      ),
    );
  }, [items, searchTerm]);

  function openDetail(request: RegistrationRequest) {
    setDetailTarget(request);
    setApproveError("");
  }

  function closeDetail() {
    setDetailTarget(null);
    setApproveError("");
    setRejectModalOpen(false);
  }

  async function handleApprove() {
    if (!detailTarget) return;
    setApproving(true);
    setApproveError("");
    try {
      const result = await registrationRequestAdminService.approve(detailTarget.id);
      setSuccessMessage(`${result.companyName} 승인 완료 (거래처코드: ${result.clientCode})`);
      closeDetail();
      await load();
    } catch (cause) {
      setApproveError(cause instanceof Error ? cause.message : "승인에 실패했습니다.");
    } finally {
      setApproving(false);
    }
  }

  async function handleReject(reason: string) {
    if (!detailTarget) return;
    setRejecting(true);
    setRejectError("");
    try {
      await registrationRequestAdminService.reject(detailTarget.id, reason);
      setSuccessMessage(`${detailTarget.companyName} 신청을 반려했습니다.`);
      closeDetail();
      await load();
    } catch (cause) {
      setRejectError(cause instanceof Error ? cause.message : "반려에 실패했습니다.");
    } finally {
      setRejecting(false);
    }
  }

  return (
    <>
      <RegistrationRequestListView
        status={status}
        onStatusChange={(value) => {
          setSuccessMessage("");
          setStatus(value);
        }}
        searchInput={searchInput}
        onSearchInputChange={setSearchInput}
        onSearchSubmit={() => setSearchTerm(searchInput)}
        items={filtered}
        loading={loading}
        loadError={loadError}
        successMessage={successMessage}
        onSelect={openDetail}
      />

      {detailTarget && (
        <RequestDetailModal
          request={detailTarget}
          approving={approving}
          approveError={approveError}
          onClose={closeDetail}
          onApprove={() => void handleApprove()}
          onReject={() => setRejectModalOpen(true)}
        />
      )}

      {detailTarget && rejectModalOpen && (
        <RejectReasonModal
          companyName={detailTarget.companyName}
          submitting={rejecting}
          error={rejectError}
          onClose={() => setRejectModalOpen(false)}
          onConfirm={(reason) => void handleReject(reason)}
        />
      )}
    </>
  );
}