"use client";

import type { ReactNode } from "react";
import { RoleLayout } from "@/features/admin/components/RoleLayout";
import {
  LayoutDashboard,
  ClipboardList,
  Factory,
  BookOpen,
  BarChart2,
  Users,
  PlusCircle,
  Home,
  Building2,
} from "lucide-react";

const NAV_ITEMS = [
  { href: "/dashboard", label: "대시보드", icon: LayoutDashboard },
  { href: "/orders", label: "수주 관리", icon: ClipboardList },
  { href: "/production", label: "생산 관리", icon: Factory },
  { href: "/catalog", label: "카탈로그", icon: BookOpen },
  { href: "/statistics", label: "통계", icon: BarChart2 },
  { href: "/admin/members", label: "멤버 관리", icon: Users },
];

const CLIENT_NAV_ITEMS = [
  { href: "/orders/new", label: "수주 등록", icon: PlusCircle },
  { href: "/my-orders", label: "내 수주 현황", icon: ClipboardList },
];

const UNREGISTERED_CLIENT_NAV_ITEMS = [
  { href: "/welcome", label: "홈", icon: Home },
  { href: "/client-registration", label: "거래처 등록", icon: Building2 },
];

export default function MainLayout({ children }: { children: ReactNode }) {
  return (
    <RoleLayout
      requiredRoles={["STAFF", "UNREGISTERED_CLIENT", "CLIENT", "ADMIN"]}
      brandSub="생산관리"
      navItems={NAV_ITEMS}
      clientNavItems={CLIENT_NAV_ITEMS}
      unregisteredClientNavItems={UNREGISTERED_CLIENT_NAV_ITEMS}
      storageKey="prodio-sidebar-collapsed"
    >
      {children}
    </RoleLayout>
  );
}
