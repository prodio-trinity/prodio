/** 백엔드 UserRole enum(STAFF, ADMIN)과 동일한 순서. */
export const MEMBER_ROLES = ["STAFF", "ADMIN"] as const;
export type MemberRole = (typeof MEMBER_ROLES)[number];

/** 관리자가 UI에서 토글할 수 있는 권한 목록. */
export const ASSIGNABLE_MEMBER_ROLES = [
  "STAFF",
  "ADMIN",
] as const satisfies readonly MemberRole[];

export type MemberStatus = "ACTIVE" | "INACTIVE";

export interface AdminMember {
  id: string;
  email: string;
  name: string;
  status: MemberStatus;
  roles: MemberRole[];
}

export interface AdminMemberPage {
  members: AdminMember[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface MemberFilters {
  query: string;
  page: number;
  size: number;
}

export interface UpdateRolesCommand {
  id: string;
  roles: MemberRole[];
}
