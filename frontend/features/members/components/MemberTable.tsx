"use client";

import { useEffect, useMemo, useState } from "react";
import { RefreshCw, Search, UserCog } from "lucide-react";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { queryErrorMessage } from "@/features/store/api/queryError";
import {
  useGetMembersQuery,
  useUpdateMemberRolesMutation,
} from "../api/membersApi";
import {
  ASSIGNABLE_MEMBER_ROLES,
  type AdminMember,
  type MemberFilters,
  type MemberRole,
} from "../types/member";
import styles from "./MemberTable.module.css";

const PAGE_SIZE = 20;

function initialFilters(): MemberFilters {
  return { query: "", page: 0, size: PAGE_SIZE };
}

function sameRoles(left: MemberRole[], right: MemberRole[]): boolean {
  if (left.length !== right.length) return false;
  const rightSet = new Set(right);
  return left.every((role) => rightSet.has(role));
}

export function MemberTable() {
  const [filters, setFilters] = useState<MemberFilters>(initialFilters);
  const [searchInput, setSearchInput] = useState("");
  const [edits, setEdits] = useState<Record<string, MemberRole[]>>({});
  const [savingId, setSavingId] = useState<string | null>(null);
  const [rowError, setRowError] = useState<Record<string, string>>({});

  const { user } = useAuth();
  const currentUserId = user?.id ?? "";

  const { data, isFetching, error } = useGetMembersQuery(filters);
  const [updateRoles] = useUpdateMemberRolesMutation();

  // 검색어 디바운스 — 타이핑마다 요청을 보내지 않는다.
  useEffect(() => {
    const timer = window.setTimeout(() => {
      setFilters((current) => ({
        ...current,
        query: searchInput.trim(),
        page: 0,
      }));
    }, 300);
    return () => window.clearTimeout(timer);
  }, [searchInput]);

  const members = useMemo(() => data?.members ?? [], [data]);
  const errorMessage = error
    ? queryErrorMessage(error, "회원 목록을 불러오지 못했습니다.")
    : null;

  const effectiveRoles = (member: AdminMember): MemberRole[] =>
    edits[member.id] ?? member.roles;

  const isDirty = (member: AdminMember): boolean =>
    edits[member.id] !== undefined &&
    !sameRoles(edits[member.id], member.roles);

  /**
   * 본인의 ADMIN 은 스스로 해제할 수 없다. 서버(AdminMemberService)가 어차피 ADMIN_SELF_DEMOTION
   * 으로 막지만, 프론트에서 먼저 막아서 실패가 뻔한 요청을 안 보내고 이유도 바로 보여준다.
   */
  const isLocked = (member: AdminMember, role: MemberRole): boolean =>
    role === "ADMIN" && member.id === currentUserId;

  const toggleRole = (member: AdminMember, role: MemberRole) => {
    if (isLocked(member, role)) return;
    const current = effectiveRoles(member);
    const next = current.includes(role)
      ? current.filter((value) => value !== role)
      : [...current, role];
    setEdits((previous) => ({ ...previous, [member.id]: next }));
  };

  const resetRow = (member: AdminMember) => {
    setEdits((previous) => {
      const next = { ...previous };
      delete next[member.id];
      return next;
    });
    setRowError((previous) => ({ ...previous, [member.id]: "" }));
  };

  const saveRow = async (member: AdminMember) => {
    setSavingId(member.id);
    setRowError((previous) => ({ ...previous, [member.id]: "" }));
    try {
      await updateRoles({
        id: member.id,
        roles: effectiveRoles(member),
      }).unwrap();
      resetRow(member);
    } catch (reason) {
      setRowError((previous) => ({
        ...previous,
        [member.id]: queryErrorMessage(reason, "권한 변경에 실패했습니다."),
      }));
    } finally {
      setSavingId(null);
    }
  };

  const goToPage = (page: number) => {
    setFilters((current) => ({ ...current, page }));
  };

  const totalPages = data?.totalPages ?? 0;

  return (
    <section className={styles.page}>
      <header className={styles.hero}>
        <div>
          <p className={styles.eyebrow}>
            <UserCog size={15} /> Members
          </p>
          <h1>회원 관리</h1>
          <p>
            이름 또는 이메일로 회원을 검색하고 권한을 변경합니다. 권한이 없으면
            신규 회원에게는 기본적으로 PENDING 권한이 부여됩니다.
          </p>
        </div>
        {data ? (
          <span className={styles.total}>
            총 {data.totalElements.toLocaleString()}명
          </span>
        ) : null}
      </header>

      <form
        className={styles.filters}
        onSubmit={(event) => {
          event.preventDefault();
          setFilters((current) => ({
            ...current,
            query: searchInput.trim(),
            page: 0,
          }));
        }}
      >
        <label>
          <span>검색어</span>
          <input
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="이름 또는 이메일"
          />
        </label>
        <button
          className={styles.searchButton}
          type="submit"
          disabled={isFetching}
        >
          {isFetching ? (
            <RefreshCw className={styles.spinning} size={17} />
          ) : (
            <Search size={17} />
          )}
          {isFetching ? "조회 중" : "검색"}
        </button>
      </form>

      {errorMessage ? <div className={styles.error}>{errorMessage}</div> : null}

      <article className={styles.panel}>
        {members.length ? (
          <>
            <div className={styles.tableWrap}>
              <table>
                <thead>
                  <tr>
                    <th>회원</th>
                    <th>상태</th>
                    {ASSIGNABLE_MEMBER_ROLES.map((role) => (
                      <th key={role} className={styles.roleCol}>
                        {role}
                      </th>
                    ))}
                    <th className={styles.actionCol}>작업</th>
                  </tr>
                </thead>
                <tbody>
                  {members.map((member) => {
                    const roles = effectiveRoles(member);
                    const dirty = isDirty(member);
                    const saving = savingId === member.id;
                    return (
                      <tr
                        key={member.id}
                        className={styles.row}
                        data-dirty={dirty}
                      >
                        <td>
                          <strong>
                            {member.name || "(이름 없음)"}
                            {member.id === currentUserId ? (
                              <span
                                className={styles.selfBadge}
                                title="본인 계정 (ADMIN 해제 불가)"
                              >
                                나
                              </span>
                            ) : null}
                          </strong>
                          <small>{member.email}</small>
                          {rowError[member.id] ? (
                            <div className={styles.rowError}>
                              {rowError[member.id]}
                            </div>
                          ) : null}
                        </td>
                        <td>
                          <span
                            className={styles.statusBadge}
                            data-status={member.status}
                          >
                            {member.status === "ACTIVE" ? "활성" : "비활성"}
                          </span>
                        </td>
                        {ASSIGNABLE_MEMBER_ROLES.map((role) => (
                          <td key={role} className={styles.roleCell}>
                            <input
                              type="checkbox"
                              checked={roles.includes(role)}
                              disabled={isLocked(member, role) || saving}
                              onChange={() => toggleRole(member, role)}
                              aria-label={`${member.email} ${role}`}
                            />
                          </td>
                        ))}
                        <td className={styles.actionCell}>
                          <button
                            type="button"
                            className={styles.saveButton}
                            disabled={!dirty || saving}
                            onClick={() => saveRow(member)}
                          >
                            {saving ? (
                              <RefreshCw
                                className={styles.spinning}
                                size={12}
                              />
                            ) : null}
                            {saving ? "저장 중" : "저장"}
                          </button>
                          {dirty ? (
                            <button
                              type="button"
                              className={styles.resetButton}
                              disabled={saving}
                              onClick={() => resetRow(member)}
                            >
                              취소
                            </button>
                          ) : null}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <div className={styles.pagination}>
              <button
                type="button"
                onClick={() => goToPage(filters.page - 1)}
                disabled={filters.page <= 0 || isFetching}
              >
                이전
              </button>
              <span>
                {filters.page + 1} / {Math.max(totalPages, 1)} 페이지
              </span>
              <button
                type="button"
                onClick={() => goToPage(filters.page + 1)}
                disabled={filters.page + 1 >= totalPages || isFetching}
              >
                다음
              </button>
            </div>
          </>
        ) : (
          <div className={styles.emptyRows}>
            {isFetching ? "불러오는 중..." : "조회된 회원이 없습니다."}
          </div>
        )}
      </article>
    </section>
  );
}
