import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { memberService } from "../services/memberService";
import type {
  AdminMember,
  AdminMemberPage,
  MemberFilters,
  UpdateRolesCommand,
} from "../types/member";

const membersApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getMembers: build.query<AdminMemberPage, MemberFilters>({
      queryFn: (filters, api) =>
        queryResult(memberService.getMembers(filters, api.signal)),
      providesTags: (result) =>
        result
          ? [
              ...result.members.map((member) => ({
                type: "Member" as const,
                id: member.id,
              })),
              { type: "Member" as const, id: "LIST" },
            ]
          : [{ type: "Member" as const, id: "LIST" }],
    }),
    updateMemberRoles: build.mutation<AdminMember, UpdateRolesCommand>({
      queryFn: (command, api) =>
        queryResult(memberService.updateRoles(command, api.signal)),
      invalidatesTags: (_result, _error, { id }) => [
        { type: "Member", id },
        { type: "Member", id: "LIST" },
      ],
    }),
  }),
});

export const { useGetMembersQuery, useUpdateMemberRolesMutation } = membersApi;
