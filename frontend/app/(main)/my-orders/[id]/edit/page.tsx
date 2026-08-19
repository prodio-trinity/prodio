import { OrderEditForm } from "@/features/orders/components/OrderForm";

export default async function MyOrderEditPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <OrderEditForm id={id} mine />;
}
