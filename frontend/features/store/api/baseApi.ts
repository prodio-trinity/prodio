import { createApi, fakeBaseQuery } from "@reduxjs/toolkit/query/react";
import type { QueryError } from "./queryError";

/**
 * Prodio REST 서버 상태의 단일 캐시.
 *
 * 도메인별 API 파일은 이 객체에 endpoint를 주입한다.
 */
export const baseApi = createApi({
  reducerPath: "prodioApi",
  baseQuery: fakeBaseQuery<QueryError>(),
  tagTypes: ["Auth", "Customer", "RepairCase", "Member", "Product", "ServiceRequest"],
  endpoints: () => ({}),
});
