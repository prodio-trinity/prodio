"use client";

import { useState } from "react";
import type { StatFilters } from "../types/stat";

const emptyFilters: StatFilters = {};

/** 조회 조건(from/to/status)의 입력값(draft)과 실제 조회에 쓰이는 값(filters)을 분리해서 관리한다. */
export function useStatFilters() {
  const [draft, setDraft] = useState<StatFilters>(emptyFilters);
  const [filters, setFilters] = useState<StatFilters>(emptyFilters);

  const submit = () => setFilters(draft);

  return { draft, setDraft, filters, submit };
}
