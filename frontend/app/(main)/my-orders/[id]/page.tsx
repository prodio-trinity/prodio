import { MyOrderDetail } from "@/features/orders/components/MyOrderDetail";

export default async function MyOrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <MyOrderDetail id={id} />;
}
