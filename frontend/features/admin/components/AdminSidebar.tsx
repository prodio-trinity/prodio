"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LogOut, PanelLeftClose, PanelLeftOpen, type LucideIcon } from "lucide-react";
import styles from "./AdminSidebar.module.css";

export interface SidebarNavItem {
  href: string;
  label: string;
  icon: LucideIcon;
}

interface AdminSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  onLogout: () => void;
  /** 사이드바 로고 아래 서브 라벨. 역할별 화면 이름 (예: "Admin", "상담원"). */
  brandSub: string;
  navItems: SidebarNavItem[];
}

export function AdminSidebar({
  isOpen,
  onClose,
  isCollapsed,
  onToggleCollapse,
  onLogout,
  brandSub,
  navItems,
}: AdminSidebarProps) {
  const pathname = usePathname();
  // href가 서로 다른 nav 항목의 prefix인 경우(예: /technicians, /technicians/products)
  // 가장 구체적인(긴) 항목 하나만 활성화한다.
  const bestMatchHref = navItems
    .map((item) => item.href)
    .filter((href) => pathname === href || pathname.startsWith(`${href}/`))
    .sort((a, b) => b.length - a.length)[0];
  const isActive = (href: string) => href === bestMatchHref;

  return (
    <>
      {isOpen ? <div onClick={onClose} className={styles.backdrop} /> : null}
      <aside
        className={`${styles.sidebar} ${isOpen ? styles.sidebarOpen : styles.sidebarClosed}`}
        data-collapsed={isCollapsed}
      >
        <div className={styles.top}>
          <div className={styles.logoArea}>
            <div className={styles.logoGroup}>
              <div className={styles.logoBadge}>P</div>
              <span className={styles.collapsible}>
                <span className={styles.logoBrand}>Prodio</span>
                <span className={styles.logoSub}>{brandSub}</span>
              </span>
            </div>
            <button
              type="button"
              onClick={onToggleCollapse}
              aria-label={isCollapsed ? "사이드바 펼치기" : "사이드바 접기"}
              className={styles.toggleButton}
            >
              {isCollapsed ? <PanelLeftOpen className={styles.toggleIcon} /> : <PanelLeftClose className={styles.toggleIcon} />}
            </button>
          </div>
          <nav className={styles.nav}>
            {navItems.map(({ href, label, icon: Icon }) => (
              <Link
                key={href}
                href={href}
                className={styles.navItem}
                data-active={isActive(href) ? "true" : "false"}
                onClick={onClose}
              >
                <Icon className={styles.icon} />
                <span className={styles.collapsible}>{label}</span>
              </Link>
            ))}
          </nav>
        </div>
        <div className={styles.bottom}>
          <button onClick={onLogout} className={styles.logoutButton}>
            <LogOut className={styles.icon} />
            <span className={styles.collapsible}>로그아웃</span>
          </button>
        </div>
      </aside>
    </>
  );
}
