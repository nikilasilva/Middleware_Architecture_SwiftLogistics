/* eslint-disable @typescript-eslint/no-explicit-any */
export interface DriverOrder {
  orderId: string;
  recipient: string;
  address: string;
  phone: string;
  packageDetails: string;
  route?: string;
  routeInfo?: string;
  status: "assigned" | "in_transit" | "delivered";
  assignedDriver?: string;
  driverStatus?: string;
  items?: Array<{
    itemId: string;
    description: string;
    quantity: number;
    weightKg: number;
  }>;
  recipientName?: string;
  recipientPhone?: string;
  deliveryAddress?: string;
  pickupAddress?: string;
  clientId?: string;
  createdAt?: string;
  assignedAt?: number;
}

export interface DriverOrdersResponse {
  success: boolean;
  driverId: string;
  orders: DriverOrder[];
  totalOrders: number;
  error?: string;
  timestamp?: number;
  source?: string;
}

// Fetch orders assigned to a driver from notification service
export async function fetchDriverOrders(
  driverId: string
): Promise<DriverOrdersResponse> {
  try {
    console.log("Fetching orders for driver:", driverId);

    const response = await fetch(
      `http://localhost:8088/api/driver/${driverId}/orders`
    );

    if (!response.ok) {
      throw new Error(
        `Failed to fetch driver orders: ${response.status} ${response.statusText}`
      );
    }

    const data = await response.json();
    console.log("Raw API response:", data);

    // Transform backend data to match our interface
    const transformedOrders: DriverOrder[] = (data.orders || []).map(
      (order: any) => {
        console.log("Transforming order:", order);

        // Extract recipient from different possible locations
        const recipient =
          order.recipientName ||
          order.recipient ||
          order.processingResult?.recipient ||
          "Unknown Recipient";

        // Extract phone from different possible locations
        const phone =
          order.recipientPhone ||
          order.phone ||
          order.processingResult?.recipientPhone ||
          "Phone not available";

        // Extract delivery address
        const address =
          order.deliveryAddress ||
          order.address ||
          order.recipientAddress ||
          order.processingResult?.deliveryAddress ||
          "Address not available";

        // Extract pickup address
        const pickupAddress =
          order.pickupAddress || order.processingResult?.pickupAddress || null;

        // Extract items from processing result
        const items = order.items || order.processingResult?.itemList || [];

        return {
          orderId: order.orderId || order.id,
          recipient: recipient,
          address: address,
          phone: phone,
          packageDetails:
            order.packageDetails || `Package for ${order.orderId || order.id}`,
          route:
            order.routeInfo ||
            `Route for ${order.orderId || order.id} - Check with dispatch`,
          routeInfo: order.routeInfo,
          status: mapBackendStatusToDriverStatus(
            order.driverStatus || order.status
          ),
          assignedDriver: order.assignedDriver,
          driverStatus: order.driverStatus,
          items: items,
          recipientName: recipient,
          recipientPhone: phone,
          deliveryAddress: address,
          pickupAddress: pickupAddress,
          clientId: order.clientId,
          createdAt: order.createdAt,
          assignedAt: order.assignedAt,
        };
      }
    );

    console.log("Transformed orders:", transformedOrders);

    return {
      success: data.success,
      driverId: data.driverId,
      orders: transformedOrders,
      totalOrders: transformedOrders.length,
      error: data.error,
      timestamp: data.timestamp,
      source: data.source,
    };
  } catch (error) {
    console.error("Error fetching driver orders:", error);
    throw error;
  }
}

// Update order status by driver
export async function updateOrderStatusByDriver(
  orderId: string,
  status: string,
  driverId: string
): Promise<{
  success: boolean;
  orderId: string;
  newStatus: string;
  message?: string;
  error?: string;
}> {
  try {
    console.log(
      `Updating order ${orderId} to status ${status} for driver ${driverId}`
    );

    const response = await fetch(
      `http://localhost:8088/api/driver/orders/${orderId}/status?status=${status}&driverId=${driverId}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error(
        `Failed to update order status: ${response.status} ${response.statusText}`
      );
    }

    const data = await response.json();
    console.log("Status update response:", data);

    return data;
  } catch (error) {
    console.error("Error updating order status:", error);
    throw error;
  }
}

// Helper function to map backend status to driver status
function mapBackendStatusToDriverStatus(
  backendStatus: string
): "assigned" | "in_transit" | "delivered" {
  if (!backendStatus) return "assigned";

  switch (backendStatus.toLowerCase()) {
    case "ready_for_dispatch":
    case "assigned":
    case "pending":
    case "confirmed":
      return "assigned";
    case "in_transit":
    case "loading":
    case "loaded":
      return "in_transit";
    case "delivered":
    case "completed":
      return "delivered";
    default:
      return "assigned";
  }
}
