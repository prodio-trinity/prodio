import { OrderEditForm } from "@/features/orders/components/OrderForm";

export default async function OrderEditPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <OrderEditForm id={id} />;
}
