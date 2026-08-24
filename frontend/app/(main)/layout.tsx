"use client";

import type { ReactNode } from "react";
import { RoleLayout } from "@/features/admin/components/RoleLayout";
import {
  LayoutDashboard,
  Activity,
  ClipboardList,
  Factory,
  ClipboardCheck,
  Users,
  PlusCircle,
  Home,
  Building2,
  Package,
} from "lucide-react";

const NAV_ITEMS = [
  { href: "/stat", label: "대시보드", icon: LayoutDashboard },
  { href: "/dashboard", label: "수주 현황", icon: Activity },
  { href: "/orders", label: "수주 관리", icon: ClipboardList },
  { href: "/production", label: "생산 관리", icon: Factory },
  { href: "/catalog/clients", label: "거래처 관리", icon: Building2 },
  { href: "/catalog/products", label: "품목 관리", icon: Package },
  { href: "/catalog/registration", label: "등록 신청 관리", icon: ClipboardCheck },
  { href: "/admin/members", label: "멤버 관리", icon: Users },
];

const CLIENT_NAV_ITEMS = [
  { href: "/orders/new", label: "수주 등록", icon: PlusCircle },
  { href: "/my-orders", label: "내 수주 현황", icon: ClipboardList },
];

const PENDING_NAV_ITEMS = [
  { href: "/pending", label: "홈", icon: Home },
  { href: "/pending/registration", label: "거래처 등록", icon: Building2 },
];

// catalog 모듈의 ADMIN 전용 화면
const CATALOG_ADMIN_ONLY_PREFIXES = [
  "/catalog/clients",
  "/catalog/products",
  "/catalog/registration",
  "/catalog/categories",
];

export default function MainLayout({ children }: { children: ReactNode }) {
  return (
    <RoleLayout
      requiredRoles={["PENDING", "CLIENT", "ADMIN"]}
      brandSub="생산관리"
      navItems={NAV_ITEMS}
      clientNavItems={CLIENT_NAV_ITEMS}
      pendingNavItems={PENDING_NAV_ITEMS}
      storageKey="prodio-sidebar-collapsed"
      adminOnlyPrefixes={CATALOG_ADMIN_ONLY_PREFIXES}
    >
      {children}
    </RoleLayout>
  );
}
