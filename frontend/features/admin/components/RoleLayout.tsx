"use client";

import { useEffect, useState, type ReactNode } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useLogoutMutation } from "@/features/auth/api/authApi";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { AdminHeader } from "./AdminHeader";
import { AdminSidebar, type SidebarNavItem } from "./AdminSidebar";
import styles from "./RoleLayout.module.css";

interface RoleLayoutProps {
  children: ReactNode;
  /** 이 화면에 들어올 수 있는 권한 목록. 이 중 하나라도 있으면 통과. */
  requiredRoles: string[];
  /** 사이드바 로고 아래 서브 라벨 (예: "Admin", "상담원"). */
  brandSub: string;
  navItems: SidebarNavItem[];
  /** ADMIN 권한이 있을 때만 추가로 노출할 메뉴. */
  adminNavItems?: SidebarNavItem[];
  /** 사이드바 접힘 상태를 저장할 localStorage 키. 화면마다 따로 기억하도록 구분한다. */
  storageKey: string;
  /** 권한이 없을 때 돌려보낼 경로. */
  deniedRedirect?: string;
}

/**
 * 관리자 사이드바 구조(AdminSidebar + AdminHeader)를 역할별 화면(관리자/상담원/기사/개발자)에서
 * 공통으로 재사용하기 위한 레이아웃 셸. 화면마다 필요한 권한·메뉴·브랜드 라벨만 다르다.
 */
export function RoleLayout({
  children,
  requiredRoles,
  brandSub,
  navItems,
  adminNavItems,
  storageKey,
  deniedRedirect = "/",
}: RoleLayoutProps) {
  const router = useRouter();
  const pathname = usePathname();
  const { status: authStatus, user, error: authError } = useAuth();
  const [logout] = useLogoutMutation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const hasRequiredRole = user?.roles.some((role) => requiredRoles.includes(role)) ?? false;
  const isAdmin = user?.roles.includes("ADMIN") ?? false;

  // 현재 경로가 관리자 전용 영역인지 판단
  const adminPrefixes = (adminNavItems ?? []).map((item) => item.href);
  const isInAdminArea = adminPrefixes.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`),
  );

  // 관리자 영역이면 adminNavItems만, 아니면 navItems만 표시
  const activeNavItems = isAdmin && isInAdminArea && adminNavItems ? adminNavItems : navItems;

  useEffect(() => {
    if (authStatus === "unauthenticated") router.replace("/login");
    else if (authStatus === "authenticated" && !hasRequiredRole) router.replace(deniedRedirect);
  }, [authStatus, hasRequiredRole, router, deniedRedirect]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      if (window.innerWidth >= 1024) setSidebarOpen(true);
      const saved = localStorage.getItem(storageKey);
      if (saved !== null) setIsSidebarCollapsed(saved === "true");
    }, 0);
    return () => window.clearTimeout(timer);
  }, [storageKey]);

  const handleToggleCollapse = () => {
    const collapsed = !isSidebarCollapsed;
    setIsSidebarCollapsed(collapsed);
    localStorage.setItem(storageKey, String(collapsed));
  };

  const handleLogout = async () => {
    await logout();
    router.replace("/login");
  };

  return (
    <div className={styles.shell}>
      {authStatus === "loading" || authStatus === "unauthenticated" ? (
        <div className={styles.state}>로그인 확인 중...</div>
      ) : authStatus === "error" ? (
        <div className={styles.state}>
          <p className={styles.stateTitle}>로그인 상태를 확인할 수 없습니다.</p>
          <p className={styles.stateDetail}>{authError}</p>
        </div>
      ) : !hasRequiredRole ? (
        <div className={styles.state}>다른 화면으로 이동 중...</div>
      ) : (
        <>
          <AdminSidebar
            isOpen={sidebarOpen}
            onClose={() => setSidebarOpen(false)}
            isCollapsed={isSidebarCollapsed}
            onToggleCollapse={handleToggleCollapse}
            onLogout={handleLogout}
            brandSub={brandSub}
            navItems={activeNavItems}
          />
          <div className={styles.main} data-collapsed={isSidebarCollapsed}>
            <AdminHeader
              onMenuToggle={() => setSidebarOpen(!sidebarOpen)}
              user={user}
              onLogout={handleLogout}
            />
            <main className={styles.content}>{children}</main>
          </div>
        </>
      )}
    </div>
  );
}
