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

// Get order status function
export async function getOrderStatus(orderId: string): Promise<{
  success: boolean;
  orderId: string;
  cmsStatus?: string;
  wmsStatus?: string;
  rosStatus?: string;
  overallStatus?: string;
  customerMessage?: string;
  warehouseInfo?: string;
  routeInfo?: string;
  clientInfo?: string;
  estimatedDelivery?: string;
  lastUpdated?: string;
  queriedBy?: string;
  queryTimestamp?: number;
  error?: string;
}> {
  const res = await fetch(`http://localhost:8089/api/orders/${orderId}/status`);

  if (!res.ok) {
    throw new Error(
      `Failed to get order status: ${res.status} ${res.statusText}`
    );
  }

  return res.json();
}
