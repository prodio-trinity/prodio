import type { StatFilters } from "../types/stat";

function toDateString(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function startOfWeek(date: Date): Date {
  const day = date.getDay();
  const diffToMonday = day === 0 ? -6 : 1 - day;
  const result = new Date(date);
  result.setDate(date.getDate() + diffToMonday);
  return result;
}

export function todayString(): string {
  return toDateString(new Date());
}

export function thisMonthRange(): StatFilters {
  const now = new Date();
  return {
    from: toDateString(new Date(now.getFullYear(), now.getMonth(), 1)),
    to: toDateString(now),
  };
}

export function thisWeekRange(): StatFilters {
  const now = new Date();
  return {
    from: toDateString(startOfWeek(now)),
    to: toDateString(now),
  };
}

export function lastMonthRange(): StatFilters {
  const now = new Date();
  return {
    from: toDateString(new Date(now.getFullYear(), now.getMonth() - 1, 1)),
    to: toDateString(new Date(now.getFullYear(), now.getMonth(), 0)),
  };
}
