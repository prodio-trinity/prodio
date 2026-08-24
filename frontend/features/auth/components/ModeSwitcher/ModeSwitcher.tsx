"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./ModeSwitcher.module.css";

interface ModeSwitcherProps {
  roles: string[];
  compact?: boolean;
}

interface RoleDestination {
  label: string;
  href: string;
  /** 이 역할 영역으로 간주할 경로 프리픽스 목록. 하나라도 일치하면 활성. */
  activeWhen: string[];
}

const ROLE_DESTINATIONS: Record<string, RoleDestination> = {
  PENDING: {
    label: "가입자",
    href: "/pending",
    activeWhen: ["/pending"],
  },
  CLIENT: {
    label: "거래처",
    href: "/my-orders",
    activeWhen: ["/my-orders", "/orders/new"],
  },
  ADMIN: {
    label: "관리자",
    href: "/stat",
    activeWhen: ["/orders", "/production", "/catalog", "/stat", "/admin"],
  },
};

function isAreaActive(pathname: string, prefixes: string[]): boolean {
  return prefixes.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`),
  );
}

export function ModeSwitcher({ roles, compact = false }: ModeSwitcherProps) {
  const pathname = usePathname();

  const buttons = Array.from(new Set(roles))
    .map((role) => ({ role, destination: ROLE_DESTINATIONS[role] }))
    .filter(
      (entry): entry is { role: string; destination: RoleDestination } =>
        Boolean(entry.destination),
    );

  if (buttons.length < 1) return null;

  // CLIENT 경로가 매칭되면 ADMIN은 active 아님 (경로 겹침 방지)
  const clientButton = buttons.find((b) => b.role === "CLIENT");
  const isClientArea = clientButton
    ? isAreaActive(pathname, clientButton.destination.activeWhen)
    : false;

  return (
    <nav aria-label="화면 전환" className={styles.switcher} data-compact={compact}>
      {buttons.map(({ role, destination }) => {
        const isAdmin = role === "ADMIN";
        const active = isAdmin
          ? !isClientArea && isAreaActive(pathname, destination.activeWhen)
          : isAreaActive(pathname, destination.activeWhen);
        return (
          <Link
            key={role}
            href={destination.href}
            aria-current={active ? "page" : undefined}
            className={styles.link}
          >
            {destination.label}
          </Link>
        );
      })}
    </nav>
  );
}
