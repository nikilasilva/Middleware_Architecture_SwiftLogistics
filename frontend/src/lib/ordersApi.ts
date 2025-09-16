import { OrdersApiResponse } from "@/types/order";

export async function fetchOrdersByClient(
  clientId: string
): Promise<OrdersApiResponse> {
  const res = await fetch(
    `http://localhost:8089/api/orders/client/${clientId}`
  );
  if (!res.ok) {
    throw new Error(`Failed to fetch orders: ${res.status} ${res.statusText}`);
  }
  return res.json();
}

// Cancel order function
export async function cancelOrder(orderId: string): Promise<{
  success: boolean;
  orderId: string;
  message?: string;
  error?: string;
  status?: string;
  cancelledAt?: number;
}> {
  const res = await fetch(
    `http://localhost:8089/api/orders/${orderId}/cancel`,
    {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
    }
  );

  if (!res.ok) {
    throw new Error(`Failed to cancel order: ${res.status} ${res.statusText}`);
  }

  return res.json();
}
