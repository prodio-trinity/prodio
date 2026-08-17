import { baseApi } from "@/features/store/api/baseApi";
import { queryResult } from "@/features/store/api/queryError";
import { authService } from "../services/authService";
import type { CurrentUser } from "../types/auth";

const authApi = baseApi.injectEndpoints({
  endpoints: (build) => ({
    getCurrentUser: build.query<CurrentUser | null, void>({
      queryFn: (_argument, api) =>
        queryResult(authService.getCurrentUser(api.signal)),
      providesTags: ["Auth"],
    }),
    login: build.mutation<CurrentUser, { email: string; password: string }>({
      queryFn: ({ email, password }, api) =>
        queryResult(authService.login(email, password, api.signal)),
      invalidatesTags: ["Auth"],
    }),
    signup: build.mutation<CurrentUser, { email: string; password: string; name: string }>({
      queryFn: ({ email, password, name }, api) =>
        queryResult(authService.signup(email, password, name, api.signal)),
      invalidatesTags: ["Auth"],
    }),
    logout: build.mutation<void, void>({
      queryFn: (_argument, api) => queryResult(authService.logout(api.signal)),
      invalidatesTags: ["Auth"],
    }),
  }),
});

export const {
  useGetCurrentUserQuery,
  useLoginMutation,
  useSignupMutation,
  useLogoutMutation,
} = authApi;
