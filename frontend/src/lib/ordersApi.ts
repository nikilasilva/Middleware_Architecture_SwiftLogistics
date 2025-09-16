import { OrdersApiResponse } from "@/types/order";

export async function fetchOrdersByClient(clientId: string): Promise<OrdersApiResponse> {
  const res = await fetch(`http://localhost:8089/api/orders/client/${clientId}`);
  if (!res.ok) {
    throw new Error(`Failed to fetch orders: ${res.status} ${res.statusText}`);
  }
  return res.json();
}