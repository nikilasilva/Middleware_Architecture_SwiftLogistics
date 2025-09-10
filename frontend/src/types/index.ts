export interface SystemHealth {
  cms?: {
    status: string;
    orders_count: number;
    clients_count: number;
    protocol: string;
    service: string;
    timestamp: string;
  };
  ros?: {
    status: string;
    active_routes: number;
    available_vehicles: number;
    total_vehicles: number;
    protocol: string;
    service: string;
    timestamp: string;
  };
  wms?: {
    status: string;
    total_packages: number;
    zones: Record<string, any>;
  };
}

export interface OrderData {
  clientId: string;
  recipientName: string;
  recipientAddress: string;
  recipientPhone: string;
  packageDetails: string;
}

export interface Vehicle {
  vehicle_id: string;
  driver_name: string;
  vehicle_type: string;
  capacity: number;
  current_location: {
    lat: number;
    lng: number;
  };
  status: 'available' | 'busy' | 'assigned';
}

export interface RouteOptimization {
  vehicle_id: string;
  delivery_addresses: Array<{
    address: string;
    lat: number;
    lng: number;
    order_id: string;
  }>;
  priority?: 'low' | 'normal' | 'high';
}

export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}