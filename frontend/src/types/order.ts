export interface ApiOrderItem {
  itemId: string;
  description: string;
  quantity: number;
  weightKg: number;
}

export interface ApiOrder {
  orderId: string;
  internalOrderId?: string;
  status: string;
  createdAt: string;
  recipient: string;
  recipientName?: string;
  address: string;
  recipientAddress?: string;
  phone?: string;
  recipientPhone?: string;
  totalWeight: number;
  totalItems: number;
  billingAmount: number;
  packageDetails: string;
  items?: ApiOrderItem[];
  routeStatus?: string;
  packageStatus?: string;
  overallStatus?: string;
  clientId?: string;
  trackingNumber?: string;
  driverName?: string;
  vehicleId?: string;
  estimatedDelivery?: string;
  notes?: string;
}

export interface OrdersApiResponse {
  success: boolean;
  clientId: string;
  orders: ApiOrder[];
  totalOrders: number;
  dataSource: string;
  timestamp: number;
}
