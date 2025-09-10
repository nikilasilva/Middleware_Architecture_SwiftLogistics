"use client";

import { useState, useEffect } from "react";

interface Order {
  id: string;
  clientId: string;
  recipientName: string;
  recipientAddress: string;
  recipientPhone: string;
  packageDetails: string;
  status: "pending" | "assigned" | "in_transit" | "delivered" | "cancelled";
  createdAt: string;
  estimatedDelivery?: string;
  trackingNumber: string;
  driverName?: string;
  vehicleId?: string;
}

interface TrackOrdersProps {
  onBack: () => void;
}

export default function TrackOrders({ onBack }: TrackOrdersProps) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      // Replace with actual API call
      const mockOrders: Order[] = [
        {
          id: "1",
          clientId: "techmart",
          recipientName: "John Doe",
          recipientAddress: "123 Main St, Colombo",
          recipientPhone: "+94 77 123 4567",
          packageDetails: "Electronics - Laptop",
          status: "in_transit",
          createdAt: "2024-01-15T10:30:00Z",
          estimatedDelivery: "2024-01-16T15:00:00Z",
          trackingNumber: "SL001234",
          driverName: "Saman Silva",
          vehicleId: "VH001",
        },
        {
          id: "2",
          clientId: "fashionhub",
          recipientName: "Jane Smith",
          recipientAddress: "456 Galle Road, Kandy",
          recipientPhone: "+94 71 987 6543",
          packageDetails: "Clothing - T-shirts (3)",
          status: "delivered",
          createdAt: "2024-01-14T08:15:00Z",
          estimatedDelivery: "2024-01-15T12:00:00Z",
          trackingNumber: "SL001235",
          driverName: "Kamal Perera",
          vehicleId: "VH002",
        },
        {
          id: "3",
          clientId: "techmart",
          recipientName: "Bob Johnson",
          recipientAddress: "789 Hill Street, Galle",
          recipientPhone: "+94 75 555 1234",
          packageDetails: "Mobile Phone Case",
          status: "pending",
          createdAt: "2024-01-16T14:20:00Z",
          trackingNumber: "SL001236",
        },
      ];
      setOrders(mockOrders);
    } catch (error) {
      console.error("Error fetching orders:", error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "delivered":
        return "bg-green-100 text-green-800";
      case "in_transit":
        return "bg-blue-100 text-blue-800";
      case "assigned":
        return "bg-yellow-100 text-yellow-800";
      case "pending":
        return "bg-gray-100 text-gray-800";
      case "cancelled":
        return "bg-red-100 text-red-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const filteredOrders = orders.filter((order) => {
    const matchesSearch =
      order.trackingNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.recipientName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.recipientAddress.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === "all" || order.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="min-h-screen bg-white text-black">
      {/* Header */}
      <header className="bg-black shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <button
                onClick={onBack}
                className="text-white hover:text-gray-300 mr-4 transition-colors"
              >
                ← Back
              </button>
              <div className="h-8 w-8 bg-white rounded mr-3 flex items-center justify-center">
                <span className="text-black font-bold text-sm">🚚</span>
              </div>
              <h1 className="text-2xl font-bold text-white">Track Orders</h1>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Search and Filter Controls */}
        <div className="mb-8 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-black mb-2">
              Search Orders
            </label>
            <input
              type="text"
              placeholder="Search by tracking number, recipient name, or address..."
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-black mb-2">
              Filter by Status
            </label>
            <select
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-black focus:outline-none focus:ring-2 focus:ring-black focus:border-transparent"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">All Statuses</option>
              <option value="pending">Pending</option>
              <option value="assigned">Assigned</option>
              <option value="in_transit">In Transit</option>
              <option value="delivered">Delivered</option>
              <option value="cancelled">Cancelled</option>
            </select>
          </div>
        </div>

        {/* Orders List */}
        {loading ? (
          <div className="flex justify-center items-center py-12">
            <div className="text-lg">Loading orders...</div>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredOrders.length === 0 ? (
              <div className="text-center py-12">
                <div className="text-gray-500 text-lg">
                  No orders found matching your criteria
                </div>
              </div>
            ) : (
              filteredOrders.map((order) => (
                <div key={order.id} className="card p-6">
                  <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
                    {/* Order Info */}
                    <div className="lg:col-span-2">
                      <div className="flex items-center justify-between mb-2">
                        <h3 className="text-lg font-semibold">
                          Tracking: {order.trackingNumber}
                        </h3>
                        <span
                          className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(
                            order.status
                          )}`}
                        >
                          {order.status.replace("_", " ").toUpperCase()}
                        </span>
                      </div>
                      <p className="text-gray-600 mb-1">
                        <strong>Recipient:</strong> {order.recipientName}
                      </p>
                      <p className="text-gray-600 mb-1">
                        <strong>Address:</strong> {order.recipientAddress}
                      </p>
                      <p className="text-gray-600 mb-1">
                        <strong>Phone:</strong> {order.recipientPhone}
                      </p>
                      <p className="text-gray-600">
                        <strong>Package:</strong> {order.packageDetails}
                      </p>
                    </div>

                    {/* Delivery Info */}
                    <div>
                      <p className="text-gray-600 mb-1">
                        <strong>Created:</strong> {formatDate(order.createdAt)}
                      </p>
                      {order.estimatedDelivery && (
                        <p className="text-gray-600 mb-1">
                          <strong>Est. Delivery:</strong>{" "}
                          {formatDate(order.estimatedDelivery)}
                        </p>
                      )}
                      {order.driverName && (
                        <p className="text-gray-600 mb-1">
                          <strong>Driver:</strong> {order.driverName}
                        </p>
                      )}
                      {order.vehicleId && (
                        <p className="text-gray-600">
                          <strong>Vehicle:</strong> {order.vehicleId}
                        </p>
                      )}
                    </div>

                    {/* Actions */}
                    <div className="flex flex-col space-y-2">
                      <button className="btn-primary text-sm py-1">
                        View Details
                      </button>
                      <button className="btn-secondary text-sm py-1">
                        Contact Driver
                      </button>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* Summary Stats */}
        <div className="mt-12 grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-gray-50 rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-black">{orders.length}</div>
            <div className="text-sm text-gray-600">Total Orders</div>
          </div>
          <div className="bg-green-50 rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-green-600">
              {orders.filter((o) => o.status === "delivered").length}
            </div>
            <div className="text-sm text-gray-600">Delivered</div>
          </div>
          <div className="bg-blue-50 rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-blue-600">
              {orders.filter((o) => o.status === "in_transit").length}
            </div>
            <div className="text-sm text-gray-600">In Transit</div>
          </div>
          <div className="bg-yellow-50 rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-yellow-600">
              {orders.filter((o) => o.status === "pending").length}
            </div>
            <div className="text-sm text-gray-600">Pending</div>
          </div>
        </div>
      </main>
    </div>
  );
}
